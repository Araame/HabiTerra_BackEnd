package com.habiterra.property.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreatePropertyTypeRequest(
        @NotBlank @Size(max = 255) String label,
        @Size(max = 10000) String description
) {}
