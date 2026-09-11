package com.habiterra.identity.dto;
import jakarta.validation.constraints.*;
import com.habiterra.identity.entity.Role;
public record CompleteRegistrationRequest(@NotBlank @Size(max=4096) String registrationToken, @NotBlank @Size(max=100) String prenom, @NotBlank @Size(max=100) String nom, @Size(max=100) String email, @Size(max=40) String telephone, @NotBlank @Size(max=72) String password, @NotBlank @Size(max=72) String confirmPassword, @NotNull Role role, @Size(max=255) String profession, @Size(max=255) String poste) {
    @Override public String toString() { return "CompleteRegistrationRequest[REDACTED]"; }
 }
