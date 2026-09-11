package com.habiterra.identity.dto;
import io.swagger.v3.oas.annotations.media.Schema;
public record AuthenticationResponse(
    @Schema(description="JWT a saisir seul dans Authorize ; Swagger ajoute Bearer automatiquement") String accessToken,
    @Schema(example="Bearer") String tokenType,
    @Schema(description="Duree de validite en secondes", example="3600") long expiresIn,
    UserResponse user
) { }
