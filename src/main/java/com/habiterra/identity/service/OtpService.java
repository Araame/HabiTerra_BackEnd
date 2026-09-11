package com.habiterra.identity.service;
import com.habiterra.identity.dto.*;
import com.habiterra.identity.entity.*;
import com.habiterra.identity.exception.AuthException;
import com.habiterra.identity.repository.*;
import com.habiterra.shared.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.security.SecureRandom;
import java.time.*;

@Service
public class OtpService {
    private final IdentifierService identifiers;
    private final IdentifierLock lock;
    private final OtpVerificationRepository otps;
    private final UtilisateurRepository users;
    private final PasswordEncoder encoder;
    private final OtpSender sender;
    private final JwtService tokens;
    private final Clock clock;
    private final long cooldown;
    private final int hourlyLimit;
    private final SecureRandom random=new SecureRandom();
    public OtpService(IdentifierService identifiers,IdentifierLock lock,OtpVerificationRepository otps,
            UtilisateurRepository users,PasswordEncoder encoder,OtpSender sender,JwtService tokens,Clock clock,
            @Value("${auth.otp.cooldown-seconds:60}") long cooldown,@Value("${auth.otp.hourly-limit:5}") int hourlyLimit) {
        if(cooldown<1 || hourlyLimit<1)throw new IllegalArgumentException("Invalid OTP limits");
        this.identifiers=identifiers;this.lock=lock;this.otps=otps;this.users=users;this.encoder=encoder;
        this.sender=sender;this.tokens=tokens;this.clock=clock;this.cooldown=cooldown;this.hourlyLimit=hourlyLimit;
    }
    @Transactional
    public RequestOtpResponse request(String input) {
        var id=identifiers.normalize(input);lock.acquire(id.value());
        var existing=id.type()==IdentifierType.EMAIL?users.findByEmail(id.value()):users.findByTelephone(id.value());
        if(existing.isPresent())throw new AuthException(409,id.type()==IdentifierType.EMAIL?"EMAIL_ALREADY_USED":"PHONE_ALREADY_USED","Identifiant deja utilise");
        Instant now=clock.instant();
        var previous=otps.findFirstByIdentifierAndIdentifierTypeOrderByIdDesc(id.value(),id.type());
        if(previous.isPresent() && now.isBefore(previous.get().getCreatedAt().plusSeconds(cooldown)))
            throw new AuthException(429,"OTP_COOLDOWN","Veuillez patienter avant de demander un nouveau code");
        if(otps.countByIdentifierAndCreatedAtAfter(id.value(),now.minusSeconds(3600))>=hourlyLimit)
            throw new AuthException(429,"OTP_RATE_LIMIT","Limite horaire atteinte");
        previous.ifPresent(otp->otp.setInvalidated(true));
        String code;
        do {
            code=String.format(java.util.Locale.ROOT,"%06d",random.nextInt(1_000_000));
        } while(previous.isPresent() && encoder.matches(code,previous.get().getCodeHash()));
        OtpVerification otp=new OtpVerification();
        otp.setIdentifier(id.value());otp.setIdentifierType(id.type());otp.setCodeHash(encoder.encode(code));
        otp.setCreatedAt(now);otp.setExpiresAt(now.plusSeconds(300));otp.setMaxAttempts(5);
        otps.saveAndFlush(otp);
        sender.send(id.type(),id.value(),code);
        return new RequestOtpResponse("Code envoye",300,cooldown);
    }
    // Failed attempts must commit even though a business exception is returned.
    @Transactional(noRollbackFor=AuthException.class)
    public VerifyOtpResponse verify(String input,String code) {
        var id=identifiers.normalize(input);lock.acquire(id.value());
        OtpVerification otp=otps.findFirstByIdentifierAndIdentifierTypeOrderByIdDesc(id.value(),id.type())
            .orElseThrow(()->new AuthException(400,"OTP_NOT_FOUND","Code introuvable"));
        if(otp.isInvalidated())throw new AuthException(400,"OTP_INVALIDATED","Code invalide");
        if(otp.isVerified())throw new AuthException(400,"OTP_ALREADY_USED","Code deja utilise");
        if(!otp.getExpiresAt().isAfter(clock.instant()))throw new AuthException(400,"OTP_EXPIRED","Code expire");
        if(otp.getAttempts()>=otp.getMaxAttempts())throw new AuthException(429,"OTP_MAX_ATTEMPTS","Trop de tentatives");
        if(code==null || !code.matches("[0-9]{6}") || !encoder.matches(code,otp.getCodeHash())){
            otp.setAttempts(otp.getAttempts()+1);
            throw new AuthException(otp.getAttempts()>=otp.getMaxAttempts()?429:400,
                otp.getAttempts()>=otp.getMaxAttempts()?"OTP_MAX_ATTEMPTS":"INVALID_OTP","Code incorrect ou limite atteinte");
        }
        otp.setVerified(true);otp.setVerifiedAt(clock.instant());
        return new VerifyOtpResponse(tokens.registration(otp),true);
    }
}
