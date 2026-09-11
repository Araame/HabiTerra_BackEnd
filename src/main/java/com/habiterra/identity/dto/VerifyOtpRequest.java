package com.habiterra.identity.dto;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;
public record VerifyOtpRequest(
    @NotBlank @Size(max=150) @Schema(description="Email ou telephone au format international, par exemple +221771234567", example="amina@example.com") String identifier,
    @NotBlank @Pattern(regexp="[0-9]{6}") @Schema(description="Code a six chiffres recu par email ou SMS", example="012345") String otp
) {
    @Override public String toString(
) { return "VerifyOtpRequest[REDACTED]"; }
 }
