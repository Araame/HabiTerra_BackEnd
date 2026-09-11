package com.habiterra.identity.dto;
import jakarta.validation.constraints.*;
import com.habiterra.identity.entity.Role;
public record RequestOtpRequest(@NotBlank @Size(max=150) String identifier) {
    @Override public String toString() { return "RequestOtpRequest[REDACTED]"; }
 }
