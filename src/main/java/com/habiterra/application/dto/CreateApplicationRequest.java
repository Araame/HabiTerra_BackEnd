package com.habiterra.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

// Creating application model
public record CreateApplicationRequest(@NotNull @Positive Long propertyId) {}
