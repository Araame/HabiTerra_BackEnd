package com.habiterra.shared.security;
import com.habiterra.identity.entity.*;
import com.habiterra.identity.exception.AuthException;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.*;
import com.nimbusds.jwt.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.time.*;
import java.util.*;

@Service
public class JwtService {
    private final byte[] accessKey, registrationKey;
    private final long accessSeconds, registrationSeconds;
    private final Clock clock;
    public JwtService(@Value("${auth.jwt.secret}") String accessSecret,
                      @Value("${auth.registration.secret}") String registrationSecret,
                      @Value("${auth.jwt.expiration-seconds:3600}") long accessSeconds,
                      @Value("${auth.registration.expiration-seconds:600}") long registrationSeconds, Clock clock) {
        this.accessKey=decodeKey(accessSecret); this.registrationKey=decodeKey(registrationSecret);
        if (Arrays.equals(accessKey, registrationKey)) throw new IllegalArgumentException("JWT and registration keys must differ");
        if (accessSeconds<1 || registrationSeconds<1) throw new IllegalArgumentException("Invalid token lifetime");
        this.accessSeconds=accessSeconds; this.registrationSeconds=registrationSeconds; this.clock=clock;
    }
    private byte[] decodeKey(String value) {
        byte[] key=Base64.getDecoder().decode(value);
        if (key.length<32) throw new IllegalArgumentException("Token keys require at least 32 random bytes, Base64 encoded");
        return key;
    }
    public long accessSeconds() { return accessSeconds; }
    public String access(Utilisateur user) {
        return sign(new JWTClaimsSet.Builder().subject(user.getIdUtilisateur().toString())
                .claim("userId",user.getIdUtilisateur()).claim("role",user.getRole().name()), "access", accessSeconds,accessKey);
    }
    public String registration(OtpVerification otp) {
        return sign(new JWTClaimsSet.Builder().subject(otp.getIdentifier()).claim("identifier",otp.getIdentifier())
                .claim("identifierType",otp.getIdentifierType().name()).claim("otpId",otp.getId()),"registration",registrationSeconds,registrationKey);
    }
    private String sign(JWTClaimsSet.Builder claims,String purpose,long ttl,byte[] key) {
        Instant now=clock.instant();
        JWTClaimsSet payload=claims.issuer("immoker").audience("immoker-"+purpose).claim("purpose",purpose)
                .issueTime(Date.from(now)).expirationTime(Date.from(now.plusSeconds(ttl))).jwtID(UUID.randomUUID().toString()).build();
        try {
            SignedJWT jwt=new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.HS256).type(JOSEObjectType.JWT).build(),payload);
            jwt.sign(new MACSigner(key)); return jwt.serialize();
        } catch (JOSEException e) { throw new IllegalStateException("Token signing failed",e); }
    }
    public JWTClaimsSet accessClaims(String token) { return verify(token,"access",accessKey); }
    public JWTClaimsSet registrationClaims(String token) { return verify(token,"registration",registrationKey); }
    private JWTClaimsSet verify(String token,String purpose,byte[] key) {
        String prefix=purpose.equals("access")?"JWT":"REGISTRATION_TOKEN";
        int status=purpose.equals("access")?401:400;
        try {
            if (token==null || token.length()>4096) throw new IllegalArgumentException();
            SignedJWT jwt=SignedJWT.parse(token);
            if (!JWSAlgorithm.HS256.equals(jwt.getHeader().getAlgorithm()) || !jwt.verify(new MACVerifier(key))) throw new IllegalArgumentException();
            JWTClaimsSet c=jwt.getJWTClaimsSet();
            if (!"immoker".equals(c.getIssuer()) || !c.getAudience().contains("immoker-"+purpose) ||
                    !purpose.equals(c.getStringClaim("purpose")) || c.getSubject()==null || c.getIssueTime()==null ||
                    c.getIssueTime().toInstant().isAfter(clock.instant().plusSeconds(30)) || c.getExpirationTime()==null) throw new IllegalArgumentException();
            if (!c.getExpirationTime().toInstant().isAfter(clock.instant())) throw new AuthException(status,prefix+"_EXPIRED","Token expire");
            if (c.getNotBeforeTime()!=null && c.getNotBeforeTime().toInstant().isAfter(clock.instant())) throw new IllegalArgumentException();
            return c;
        } catch (AuthException e) { throw e; }
        catch (Exception e) { throw new AuthException(status,prefix+"_INVALID","Token invalide"); }
    }
}
