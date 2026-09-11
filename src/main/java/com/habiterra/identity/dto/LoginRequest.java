package com.habiterra.identity.dto;
import jakarta.validation.constraints.*;
import com.habiterra.identity.entity.Role;
public record LoginRequest(@NotBlank @Size(max=150) String identifier, @NotBlank @Size(max=72) String password) {
    @Override public String toString() { return "LoginRequest[REDACTED]"; }
 }
