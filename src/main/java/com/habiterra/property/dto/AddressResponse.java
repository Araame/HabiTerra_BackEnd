package com.habiterra.property.dto;

public record AddressResponse(Long id, String country, String city, String municipality, String neighborhood, String street, Double latitude, Double longitude) {}

