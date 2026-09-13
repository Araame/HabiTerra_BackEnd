package com.habiterra.property.dto;
import jakarta.validation.constraints.*;

public record RoomRequest(@NotBlank @Size(max = 255) String name, @NotNull @Positive Integer area,
    @Size(max = 5000) String description) {}

