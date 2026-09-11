package com.habiterra.identity.dto;
import jakarta.validation.constraints.*;
import com.habiterra.identity.entity.Role;
public record RequestOtpResponse(String message, long expiresIn, long resendAfter) { }
