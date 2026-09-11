package com.habiterra.identity.dto;
import jakarta.validation.constraints.*;
import com.habiterra.identity.entity.Role;
import io.swagger.v3.oas.annotations.media.Schema;
public record CompleteRegistrationRequest(
    @NotBlank @Size(max=4096) @Schema(description="Jeton retourne par verify-otp, reserve a complete-registration ; ne pas utiliser dans Authorize") String registrationToken,
    @NotBlank @Size(max=100) String prenom,
    @NotBlank @Size(max=100) String nom,
    @Size(max=100) @Schema(description="Doit correspondre a l identifiant verifie si OTP par email", example="amina@example.com") String email,
    @Size(max=40) @Schema(description="Format international ; doit correspondre a l identifiant verifie si OTP par SMS", example="+221771234567") String telephone,
    @NotBlank @Size(max=72) @Schema(description="Mot de passe ; inscription : 12 caracteres minimum, lettre et chiffre, 72 octets UTF-8 maximum", format="password", example="ExempleSecret123!") String password,
    @NotBlank @Size(max=72) @Schema(description="Doit correspondre au mot de passe", format="password", example="ExempleSecret123!") String confirmPassword,
    @NotNull @Schema(description="Inscription : LOCATAIRE, PROPRIETAIRE ou GERANT_AGENCE. ADMIN interdit.", example="LOCATAIRE") Role role,
    @Size(max=255) @Schema(description="Obligatoire pour LOCATAIRE et PROPRIETAIRE ; interdit pour GERANT_AGENCE", example="Enseignante") String profession,
    @Size(max=255) @Schema(description="Obligatoire pour GERANT_AGENCE ; interdit pour les autres roles") String poste
) {
    @Override public String toString(
) { return "CompleteRegistrationRequest[REDACTED]"; }
 }
