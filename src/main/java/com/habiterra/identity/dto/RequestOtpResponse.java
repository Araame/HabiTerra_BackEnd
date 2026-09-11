package com.habiterra.identity.dto;
import io.swagger.v3.oas.annotations.media.Schema;
public record RequestOtpResponse(
    String message,
    @Schema(description="Duree de validite en secondes", example="300") long expiresIn,
    @Schema(description="Delai avant renvoi en secondes", example="60") long resendAfter
) { }
