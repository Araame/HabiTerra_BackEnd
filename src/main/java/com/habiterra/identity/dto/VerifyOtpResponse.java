package com.habiterra.identity.dto;
import jakarta.validation.constraints.*;
import com.habiterra.identity.entity.Role;
public record VerifyOtpResponse(String registrationToken, boolean verified) { }
