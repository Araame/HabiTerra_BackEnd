package com.habiterra.identity.dto;
import jakarta.validation.constraints.*;
import com.habiterra.identity.entity.Role;
public record AuthenticationResponse(String accessToken, String tokenType, long expiresIn, UserResponse user) { }
