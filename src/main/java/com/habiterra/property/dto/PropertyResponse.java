package com.habiterra.property.dto;

public record PropertyResponse(Long id, String title, String description, Integer area, Integer numberOfRooms,
    Integer numberOfBedrooms, Integer numberOfBathrooms, Integer monthlyRent,
    Integer depositAmount, com.habiterra.property.entity.StatutBien status,
    Boolean furnished, Boolean sharedHousingAllowed, Integer sharedHousingCapacity,
    java.time.LocalDate availableFrom, java.time.LocalDateTime creationDate,
    PropertyTypeResponse type, AddressResponse address, java.util.List<RoomResponse> rooms,
    GalleryResponse gallery, OwnerSummaryResponse owner, AgencySummaryResponse agency) {}
