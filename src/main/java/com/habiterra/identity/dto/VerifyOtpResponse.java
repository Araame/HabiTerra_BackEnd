package com.habiterra.identity.dto;
import io.swagger.v3.oas.annotations.media.Schema;
public record VerifyOtpResponse(
    @Schema(description="Jeton retourne par verify-otp, reserve a complete-registration ; ne pas utiliser dans Authorize") String registrationToken,
    boolean verified
) { }
