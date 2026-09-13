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


//Handle logic for signing up and signing in
@Service
public class AuthService {

    // USERS TABLE ACCESS
    private final UtilisateurRepository users;
    // OTP ACCESS
    private final OtpVerificationRepository otps;
    // HANDLE THE LOGIN IDENTIFIERS (If it is by email or phone number)
    private final IdentifierService identifiers;
    // Handle avoiding simultaneously registration with the same identifier : concurrency
    private final IdentifierLock lock;
    // Handle tokens
    private final JwtService tokens;
    // Encrypt the password by BCrypt
    private final PasswordEncoder encoder;
    // Handle the login process
    private final AuthenticationManager authentication;
//   Avoid dependancy with the OS hour : useful during test
    private final Clock clock;


    public AuthService(UtilisateurRepository users,OtpVerificationRepository otps,IdentifierService identifiers,
            IdentifierLock lock,JwtService tokens,PasswordEncoder encoder,AuthenticationManager authentication,Clock clock){
        this.users=users;this.otps=otps;
        this.identifiers=identifiers;
        this.lock=lock;
        this.tokens=tokens;
        this.encoder=encoder;
        this.authentication=authentication;this.clock=clock;
    }

    @Transactional
    public AuthenticationResponse register(CompleteRegistrationRequest r) {
//        Cannot create a user with admin role
        if(r.role()==Role.ADMIN)
            throw new AuthException(403,"ADMIN_FORBIDDEN","Inscription ADMIN interdite");

//        Cannot create a user without role
        if(r.role()==null)
            throw new AuthException(400,"INVALID_ROLE","Role obligatoire");

//        Reading the registrationToken by Nimbus
        var claims=tokens.registrationClaims(r.registrationToken());

        String identifier;
        IdentifierType type;
        Long otpId;

//        Retrieving the specific infos in the registration token
        try {identifier=claims.getStringClaim("identifier");
            type=IdentifierType.valueOf(claims.getStringClaim("identifierType"));
            otpId=claims.getLongClaim("otpId");}
        catch(Exception e){
            throw new AuthException(400,"REGISTRATION_TOKEN_INVALID","Token invalide");
        }

//        If no identifier or no otp
        if(identifier==null || otpId==null)
            throw new AuthException(400,"REGISTRATION_TOKEN_INVALID","Token invalide");

//Lock the identifier for avoiding simultaneously requests with the same identifier
        lock.acquire(identifier);
//        Verify the Otp
        OtpVerification otp=otps.findById(otpId).orElseThrow(()->new AuthException(400,"REGISTRATION_TOKEN_INVALID","Token invalide"));

        if(!otp.isVerified() || otp.isInvalidated() || !identifier.equals(otp.getIdentifier()) || type!=otp.getIdentifierType())
            throw new AuthException(400,"REGISTRATION_TOKEN_INVALID","Token utilise ou invalide");

//        Extract email or phone number according to the identifier used by the user
        String email=identifiers.optional(r.email(),IdentifierType.EMAIL);
        String phone=identifiers.optional(r.telephone(),IdentifierType.TELEPHONE);

//Verify the identifierType
        if(!identifier.equals(type==IdentifierType.EMAIL?email:phone))
            throw new AuthException(400,"IDENTIFIER_MISMATCH","L'identifiant verifie doit correspondre au profil");
        required(r.prenom(),"FIRST_NAME_REQUIRED","Prenom obligatoire");required(r.nom(),"LAST_NAME_REQUIRED","Nom obligatoire");

//        Avoiding registration with email or phone already existing in the DB
        if(email!=null && users.existsByEmail(email))
            throw new AuthException(409,"EMAIL_ALREADY_USED","Email deja utilise");
        if(phone!=null && users.existsByTelephone(phone))
            throw new AuthException(409,"PHONE_ALREADY_USED","Telephone deja utilise");

//      Accepting user registration by validating the password and creating a new user
        validatePassword(r.password(),r.confirmPassword());
        Utilisateur user;


//        Creating the profile according to the role
        switch(r.role()){
            case LOCATAIRE -> {
                required(r.profession(),"PROFESSION_REQUIRED","Profession obligatoire");
                reject(r.poste(),"POSTE_NOT_ALLOWED");
                Locataire profile=new Locataire();
                profile.setProfession(r.profession().strip());
                user=profile;
            }
            case PROPRIETAIRE -> {
                required(r.profession(),"PROFESSION_REQUIRED","Profession obligatoire");
                reject(r.poste(),"POSTE_NOT_ALLOWED");
                Proprietaire profile=new Proprietaire();
                profile.setProfession(r.profession().strip());
                user=profile;
            }
            case GERANT_AGENCE -> {
                required(r.poste(),"POSTE_REQUIRED","Poste obligatoire");
                reject(r.profession(),"PROFESSION_NOT_ALLOWED");
                GerantAgence profile=new GerantAgence();
                profile.setPoste(r.poste().strip());user=profile;
            }
            default -> throw new AuthException(403,"ADMIN_FORBIDDEN","Inscription ADMIN interdite");
        }

        user.setPrenom(r.prenom().strip());
        user.setNom(r.nom().strip());
        user.setEmail(email);
        user.setTelephone(phone);
        user.setMotDePasse(encoder.encode(r.password()));
        user.setRole(r.role());
        user.setStatut(StatutUtilisateur.ACTIF);
        user.setEmailVerifie(type==IdentifierType.EMAIL);
        user.setTelephoneVerifie(type==IdentifierType.TELEPHONE);
        user.setDateCreation(LocalDateTime.now(clock));
        user.setDerniereConnexion(LocalDateTime.now(clock));
        // JOINED inheritance inserts the common row and specialized row atomically.
//        Immediatly save the user in the BD
        users.saveAndFlush(user);
//        OTP is invalide after the transaction
        otp.setInvalidated(true); // consumes the registration grant in the same transaction
        return response(user);
    }



//    Login TRANSACTION
    @Transactional
    public AuthenticationResponse login(LoginRequest r) {
//        Verify the identifier type
        var id=identifiers.normalize(r.identifier());
//        Verify the password
        if(r.password()==null || r.password().getBytes(StandardCharsets.UTF_8).length>72)
            throw new AuthException(401,"BAD_CREDENTIALS","Identifiants incorrects");

//        SpringSecurity (by UsernamePasswordAuthenticationToken will create an object with the id and password)
//        And make the credentials verification with authenticate (by comparing the encrypted password with the one saved in the BD)
        var authenticated=authentication.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(id.value(),r.password()));


        Utilisateur user=users.findById(Long.valueOf(authenticated.getName()))
            .orElseThrow(()->new AuthException(401,"BAD_CREDENTIALS","Identifiants incorrects"));
//        Veriy if the account status is "active"
        ensureActive(user);
//        Verifiy the identifier type
        boolean verified=id.type()==IdentifierType.EMAIL
                ?Boolean.TRUE.equals(user.getEmailVerifie())
                :Boolean.TRUE.equals(user.getTelephoneVerifie());
        if(!verified)
            throw new AuthException(403,"IDENTIFIER_NOT_VERIFIED","Ce canal de connexion n'est pas verifie");
        user.setDerniereConnexion(LocalDateTime.now(clock));
        return response(user);
    }




//    Returning connected user infos
    @Transactional(readOnly=true)
//    Return the connected user
    public UserResponse me(Long id) {
        return userResponse(activeUser(id));
    }
//    Search the user, verify if the account status is active and return the user details infos
    public Utilisateur activeUser(Long id){
        Utilisateur user=users.findById(id).orElseThrow(()->new AuthException(401,"USER_NOT_FOUND","Utilisateur introuvable"));
        ensureActive(user);
        return user;
    }

    //    Verify the user account status
    private void ensureActive(Utilisateur user){
        if(user.getStatut()!=StatutUtilisateur.ACTIF){
            String code=user.getStatut()==StatutUtilisateur.SUSPENDU?"ACCOUNT_SUSPENDED":
                user.getStatut()==StatutUtilisateur.DESACTIVE?"ACCOUNT_DISABLED":"ACCOUNT_PENDING";
            throw new AuthException(403,code,"Compte non autorise a se connecter");
        }
    }


//    Centralise the user authentication response
    private AuthenticationResponse response(Utilisateur u){
        return new AuthenticationResponse(tokens.access(u),"Bearer",tokens.accessSeconds(),userResponse(u));
    }

//    Centralise the userdetails infos to send after authentication
    private UserResponse userResponse(Utilisateur u){
        return new UserResponse(u.getIdUtilisateur(),u.getPrenom(),u.getNom(),u.getEmail(),u.getTelephone(),u.getRole(),u.getPhotoProfil(),
            u instanceof Locataire l?l.getProfession():u instanceof Proprietaire p?p.getProfession():null,
            u instanceof GerantAgence g?g.getPoste():null);
    }

//    Valid the user profile registration according to the role by saying that a label is required
    private void required(String value,String code,String message){
        if(value==null || value.isBlank())
            throw new AuthException(400,code,message);
    }
    //    Valid the user profile registration according to the role by saying that a label is not accepted
    private void reject(String value,String code){
        if(value!=null && !value.isBlank())
            throw new AuthException(400,code,"Champ incompatible avec le role");
    }


//    Validate the password creation
    private void validatePassword(String password,String confirmation){
        if(!Objects.equals(password,confirmation))
            throw new AuthException(400,"PASSWORD_MISMATCH","Les mots de passe ne correspondent pas");
        if(password==null || password.codePointCount(0,password.length())<12 || password.getBytes(StandardCharsets.UTF_8).length>72 || !password.matches("(?s).*[A-Za-z].*") || !password.matches("(?s).*[0-9].*"))
            throw new AuthException(400,"WEAK_PASSWORD","Mot de passe : 12 caracteres minimum, lettre et chiffre, 72 octets maximum");
    }
}
