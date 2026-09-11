package com.habiterra.identity.dto;
import jakarta.validation.constraints.*;
import com.habiterra.identity.entity.Role;
public record VerifyOtpRequest(@NotBlank @Size(max=150) String identifier, @NotBlank @Pattern(regexp="[0-9]{6}") String otp) {
    @Override public String toString() { return "VerifyOtpRequest[REDACTED]"; }
 }
