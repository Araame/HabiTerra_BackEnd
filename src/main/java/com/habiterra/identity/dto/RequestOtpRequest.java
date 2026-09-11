package com.habiterra.identity.dto;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;
public record RequestOtpRequest(
    @NotBlank @Size(max=150) @Schema(description="Email ou telephone au format international, par exemple +221771234567", example="amina@example.com") String identifier
) {
    @Override public String toString(
) { return "RequestOtpRequest[REDACTED]"; }
 }
