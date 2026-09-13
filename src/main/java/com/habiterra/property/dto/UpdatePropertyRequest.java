package com.habiterra.property.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.List;

public record UpdatePropertyRequest(
    @NotBlank @Size(max = 255) String title,
    @Size(max = 20000) String description,
    @NotNull @Positive Integer area,
    @NotNull @Min(1) Integer numberOfRooms,
    @NotNull @PositiveOrZero Integer numberOfBedrooms,
    @NotNull @PositiveOrZero Integer numberOfBathrooms,
    @NotNull @Positive Integer monthlyRent,
    @NotNull @PositiveOrZero Integer depositAmount,
    @NotNull Boolean furnished,
    @NotNull Boolean sharedHousingAllowed,
    Integer sharedHousingCapacity,
    LocalDate availableFrom,
    @NotNull @Positive Long typeId,
    @NotNull @Valid AddressRequest address,
    @Size(max = 200) List<@NotNull @Valid RoomRequest> rooms
) {}

