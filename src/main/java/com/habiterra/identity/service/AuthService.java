package com.habiterra.identity.service;
import com.habiterra.identity.dto.*;
import com.habiterra.identity.entity.*;
import com.habiterra.identity.exception.AuthException;
import com.habiterra.identity.repository.*;
import com.habiterra.shared.security.JwtService;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.Objects;

@Service
public class AuthService {
    private final UtilisateurRepository users;private final OtpVerificationRepository otps;
    private final IdentifierService identifiers;private final IdentifierLock lock;private final JwtService tokens;
    private final PasswordEncoder encoder;private final AuthenticationManager authentication;private final Clock clock;
    public AuthService(UtilisateurRepository users,OtpVerificationRepository otps,IdentifierService identifiers,
            IdentifierLock lock,JwtService tokens,PasswordEncoder encoder,AuthenticationManager authentication,Clock clock){
        this.users=users;this.otps=otps;this.identifiers=identifiers;this.lock=lock;this.tokens=tokens;
        this.encoder=encoder;this.authentication=authentication;this.clock=clock;
    }
    @Transactional
    public AuthenticationResponse register(CompleteRegistrationRequest r) {
        if(r.role()==Role.ADMIN)throw new AuthException(403,"ADMIN_FORBIDDEN","Inscription ADMIN interdite");
        if(r.role()==null)throw new AuthException(400,"INVALID_ROLE","Role obligatoire");
        var claims=tokens.registrationClaims(r.registrationToken());
        String identifier;IdentifierType type;Long otpId;
        try {identifier=claims.getStringClaim("identifier");type=IdentifierType.valueOf(claims.getStringClaim("identifierType"));otpId=claims.getLongClaim("otpId");}
        catch(Exception e){throw new AuthException(400,"REGISTRATION_TOKEN_INVALID","Token invalide");}
        if(identifier==null || otpId==null)throw new AuthException(400,"REGISTRATION_TOKEN_INVALID","Token invalide");
        lock.acquire(identifier);
        OtpVerification otp=otps.findById(otpId).orElseThrow(()->new AuthException(400,"REGISTRATION_TOKEN_INVALID","Token invalide"));
        if(!otp.isVerified() || otp.isInvalidated() || !identifier.equals(otp.getIdentifier()) || type!=otp.getIdentifierType())
            throw new AuthException(400,"REGISTRATION_TOKEN_INVALID","Token utilise ou invalide");
        String email=identifiers.optional(r.email(),IdentifierType.EMAIL);
        String phone=identifiers.optional(r.telephone(),IdentifierType.TELEPHONE);
        if(!identifier.equals(type==IdentifierType.EMAIL?email:phone))
            throw new AuthException(400,"IDENTIFIER_MISMATCH","L'identifiant verifie doit correspondre au profil");
        required(r.prenom(),"FIRST_NAME_REQUIRED","Prenom obligatoire");required(r.nom(),"LAST_NAME_REQUIRED","Nom obligatoire");
        if(email!=null && users.existsByEmail(email))throw new AuthException(409,"EMAIL_ALREADY_USED","Email deja utilise");
        if(phone!=null && users.existsByTelephone(phone))throw new AuthException(409,"PHONE_ALREADY_USED","Telephone deja utilise");
        validatePassword(r.password(),r.confirmPassword());
        Utilisateur user;
        switch(r.role()){
            case LOCATAIRE -> {
                required(r.profession(),"PROFESSION_REQUIRED","Profession obligatoire"); reject(r.poste(),"POSTE_NOT_ALLOWED");
                Locataire profile=new Locataire();profile.setProfession(r.profession().strip());user=profile;
            }
            case PROPRIETAIRE -> {
                required(r.profession(),"PROFESSION_REQUIRED","Profession obligatoire");reject(r.poste(),"POSTE_NOT_ALLOWED");
                Proprietaire profile=new Proprietaire();profile.setProfession(r.profession().strip());user=profile;
            }
            case GERANT_AGENCE -> {
                required(r.poste(),"POSTE_REQUIRED","Poste obligatoire");reject(r.profession(),"PROFESSION_NOT_ALLOWED");
                GerantAgence profile=new GerantAgence();profile.setPoste(r.poste().strip());user=profile;
            }
            default -> throw new AuthException(403,"ADMIN_FORBIDDEN","Inscription ADMIN interdite");
        }
        user.setPrenom(r.prenom().strip());user.setNom(r.nom().strip());user.setEmail(email);user.setTelephone(phone);
        user.setMotDePasse(encoder.encode(r.password()));user.setRole(r.role());user.setStatut(StatutUtilisateur.ACTIF);
        user.setEmailVerifie(type==IdentifierType.EMAIL);user.setTelephoneVerifie(type==IdentifierType.TELEPHONE);
        user.setDateCreation(LocalDateTime.now(clock));user.setDerniereConnexion(LocalDateTime.now(clock));
        // JOINED inheritance inserts the common row and specialized row atomically.
        users.saveAndFlush(user);
        otp.setInvalidated(true); // consumes the registration grant in the same transaction
        return response(user);
    }
    @Transactional
    public AuthenticationResponse login(LoginRequest r) {
        var id=identifiers.normalize(r.identifier());
        if(r.password()==null || r.password().getBytes(StandardCharsets.UTF_8).length>72)
            throw new AuthException(401,"BAD_CREDENTIALS","Identifiants incorrects");
        var authenticated=authentication.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(id.value(),r.password()));
        Utilisateur user=users.findById(Long.valueOf(authenticated.getName()))
            .orElseThrow(()->new AuthException(401,"BAD_CREDENTIALS","Identifiants incorrects"));
        ensureActive(user);
        boolean verified=id.type()==IdentifierType.EMAIL?Boolean.TRUE.equals(user.getEmailVerifie()):Boolean.TRUE.equals(user.getTelephoneVerifie());
        if(!verified)throw new AuthException(403,"IDENTIFIER_NOT_VERIFIED","Ce canal de connexion n'est pas verifie");
        user.setDerniereConnexion(LocalDateTime.now(clock));return response(user);
    }
    @Transactional(readOnly=true)
    public UserResponse me(Long id) { return userResponse(activeUser(id)); }
    public Utilisateur activeUser(Long id){
        Utilisateur user=users.findById(id).orElseThrow(()->new AuthException(401,"USER_NOT_FOUND","Utilisateur introuvable"));
        ensureActive(user);return user;
    }
    private void ensureActive(Utilisateur user){
        if(user.getStatut()!=StatutUtilisateur.ACTIF){
            String code=user.getStatut()==StatutUtilisateur.SUSPENDU?"ACCOUNT_SUSPENDED":
                user.getStatut()==StatutUtilisateur.DESACTIVE?"ACCOUNT_DISABLED":"ACCOUNT_PENDING";
            throw new AuthException(403,code,"Compte non autorise a se connecter");
        }
    }
    private AuthenticationResponse response(Utilisateur u){return new AuthenticationResponse(tokens.access(u),"Bearer",tokens.accessSeconds(),userResponse(u));}
    private UserResponse userResponse(Utilisateur u){
        return new UserResponse(u.getIdUtilisateur(),u.getPrenom(),u.getNom(),u.getEmail(),u.getTelephone(),u.getRole(),u.getPhotoProfil(),
            u instanceof Locataire l?l.getProfession():u instanceof Proprietaire p?p.getProfession():null,
            u instanceof GerantAgence g?g.getPoste():null);
    }
    private void required(String value,String code,String message){if(value==null || value.isBlank())throw new AuthException(400,code,message);}
    private void reject(String value,String code){if(value!=null && !value.isBlank())throw new AuthException(400,code,"Champ incompatible avec le role");}
    private void validatePassword(String password,String confirmation){
        if(!Objects.equals(password,confirmation))throw new AuthException(400,"PASSWORD_MISMATCH","Les mots de passe ne correspondent pas");
        if(password==null || password.codePointCount(0,password.length())<12 || password.getBytes(StandardCharsets.UTF_8).length>72 ||
                !password.matches("(?s).*[A-Za-z].*") || !password.matches("(?s).*[0-9].*"))
            throw new AuthException(400,"WEAK_PASSWORD","Mot de passe : 12 caracteres minimum, lettre et chiffre, 72 octets maximum");
    }
}
