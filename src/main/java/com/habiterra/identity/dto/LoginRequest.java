package com.habiterra.identity.dto;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;
public record LoginRequest(
    @NotBlank @Size(max=150) @Schema(description="Email ou telephone au format international, par exemple +221771234567", example="amina@example.com") String identifier,
    @NotBlank @Size(max=72) @Schema(description="Mot de passe ; inscription : 12 caracteres minimum, lettre et chiffre, 72 octets UTF-8 maximum", format="password", example="ExempleSecret123!") String password
) {
    @Override public String toString(
) { return "LoginRequest[REDACTED]"; }
 }
