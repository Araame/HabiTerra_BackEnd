package com.habiterra.identity.controller;
import com.habiterra.identity.dto.*;
import com.habiterra.identity.service.*;
import com.habiterra.shared.exception.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


@Tag(name="Authentication", description="OTP, inscription et authentification JWT")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final OtpService otp;
    private final AuthService auth;
    public AuthController(OtpService otp,AuthService auth){
        this.otp=otp;
        this.auth=auth;
    }


    @Operation(summary="Demander un code OTP")
    @SecurityRequirements
    @ApiResponses({
        @ApiResponse(responseCode="200", description="Code envoye"),
        @ApiResponse(responseCode="400", description="Requete ou identifiant invalide", content=@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))),
        @ApiResponse(responseCode="409", description="Identifiant deja utilise", content=@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))),
        @ApiResponse(responseCode="429", description="Delai ou limite de demandes atteint", content=@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))),
        @ApiResponse(responseCode="503", description="Envoi OTP indisponible", content=@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class)))
    })
    @PostMapping("/request-otp")
    public RequestOtpResponse request(@Valid @RequestBody RequestOtpRequest r){
        return otp.request(r.identifier());
    }


    @Operation(summary="Renvoyer un code OTP")
    @SecurityRequirements
    @ApiResponses({
        @ApiResponse(responseCode="200", description="Nouveau code envoye"),
        @ApiResponse(responseCode="400", description="Requete ou identifiant invalide", content=@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))),
        @ApiResponse(responseCode="409", description="Identifiant deja utilise", content=@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))),
        @ApiResponse(responseCode="429", description="Delai ou limite de demandes atteint", content=@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))),
        @ApiResponse(responseCode="503", description="Envoi OTP indisponible", content=@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class)))
    })
    @PostMapping("/resend-otp")
    public RequestOtpResponse resend(@Valid @RequestBody ResendOtpRequest r){
        return otp.request(r.identifier());
    }



    @Operation(summary="Verifier le code OTP")
    @SecurityRequirements
    @ApiResponses({
        @ApiResponse(responseCode="200", description="Code verifie et jeton d'inscription retourne"),
        @ApiResponse(responseCode="400", description="Requete ou code invalide, expire ou deja utilise", content=@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))),
        @ApiResponse(responseCode="429", description="Limite de tentatives atteinte", content=@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class)))
    })
    @PostMapping("/verify-otp")
    public VerifyOtpResponse verify(@Valid @RequestBody VerifyOtpRequest r){
        return otp.verify(r.identifier(),r.otp());
    }


    @Operation(summary="Terminer l'inscription")
    @SecurityRequirements
    @ApiResponses({
        @ApiResponse(responseCode="201", description="Compte cree et JWT retourne"),
        @ApiResponse(responseCode="400", description="Profil, mot de passe ou jeton d'inscription invalide", content=@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))),
        @ApiResponse(responseCode="403", description="Inscription ADMIN interdite", content=@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))),
        @ApiResponse(responseCode="409", description="Email ou telephone deja utilise", content=@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))),
        @ApiResponse(responseCode="429", description="Limite de requetes atteinte", content=@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class)))
    })
    @PostMapping("/complete-registration")
    public ResponseEntity<AuthenticationResponse> register(@Valid @RequestBody CompleteRegistrationRequest r){
        return ResponseEntity.status(201).body(auth.register(r));
    }


    @Operation(summary="Se connecter")
    @SecurityRequirements
    @ApiResponses({
        @ApiResponse(responseCode="200", description="Authentification reussie"),
        @ApiResponse(responseCode="400", description="Requete ou identifiant invalide", content=@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))),
        @ApiResponse(responseCode="401", description="Identifiants incorrects", content=@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))),
        @ApiResponse(responseCode="403", description="Compte ou canal de connexion non autorise", content=@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))),
        @ApiResponse(responseCode="429", description="Limite de requetes atteinte", content=@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class)))
    })
    @PostMapping("/login")
    public AuthenticationResponse login(@Valid @RequestBody LoginRequest r){
        return auth.login(r);
    }

    @Operation(summary="Consulter le profil de l'utilisateur connecte")
    @ApiResponses({
        @ApiResponse(responseCode="200", description="Profil de l'utilisateur"),
        @ApiResponse(responseCode="401", description="JWT absent, invalide, expire ou utilisateur introuvable", content=@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))),
        @ApiResponse(responseCode="403", description="Compte non actif", content=@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class)))
    })
    @GetMapping("/me")
    public UserResponse me(Authentication authentication){
        return auth.me(Long.valueOf(authentication.getName()));
    }



    @Operation(summary="Se deconnecter")
    @ApiResponses({
        @ApiResponse(responseCode="204", description="Le client supprime son token ; le JWT reste valide jusqu a expiration", content=@Content),
        @ApiResponse(responseCode="401", description="JWT absent, invalide, expire ou utilisateur introuvable", content=@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))),
        @ApiResponse(responseCode="403", description="Compte non actif", content=@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))),
        @ApiResponse(responseCode="429", description="Limite de requetes atteinte", content=@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class)))
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(){
        return ResponseEntity.noContent().build();
    }
}
