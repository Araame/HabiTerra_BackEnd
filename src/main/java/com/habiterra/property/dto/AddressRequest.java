package com.habiterra.property.dto;
import jakarta.validation.constraints.*;

public record AddressRequest(@NotBlank @Size(max = 255) String country, @NotBlank @Size(max = 255) String city,
    @Size(max = 255) String municipality, @Size(max = 255) String neighborhood,
    @Size(max = 255) String street, @DecimalMin("-90") @DecimalMax("90") Double latitude,
    @DecimalMin("-180") @DecimalMax("180") Double longitude) {}

