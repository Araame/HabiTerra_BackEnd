package com.habiterra.property.dto;

import com.habiterra.property.exception.PropertyException;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record PropertyFilterRequest(
    @Size(max = 255) @Schema(description = "Exact country, case insensitive; blank ignored") String country,
    @Size(max = 255) @Schema(description = "Exact city, case insensitive; blank ignored") String city,
    @Size(max = 255) @Schema(description = "Exact municipality (commune), case insensitive; blank ignored") String municipality,
    @Size(max = 255) @Schema(description = "Exact neighborhood (quartier), case insensitive; blank ignored") String neighborhood,
    @Positive @Schema(description = "Property type id; unknown positive id returns an empty page") Long typeId,
    @PositiveOrZero @Schema(description = "Inclusive minimum monthly rent") Integer minRent,
    @PositiveOrZero @Schema(description = "Inclusive maximum monthly rent; must be >= minRent") Integer maxRent,
    @PositiveOrZero @Schema(description = "Exact number of rooms") Integer rooms,
    @PositiveOrZero @Schema(description = "Exact number of bedrooms") Integer bedrooms,
    @PositiveOrZero @Schema(description = "Exact number of bathrooms") Integer bathrooms,
    @PositiveOrZero @Schema(description = "Inclusive minimum area; integer") Integer minArea,
    @PositiveOrZero @Schema(description = "Inclusive maximum area; integer >= minArea") Integer maxArea,
    @Schema(description = "Filter furnished properties; omitted means no restriction") Boolean furnished,
    @Schema(description = "Filter shared housing permission; omitted means no restriction") Boolean sharedHousingAllowed,
    @Positive @Schema(description = "Minimum shared housing capacity; requires sharedHousingAllowed=true") Integer minSharedHousingCapacity,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Schema(description = "Inclusive latest availability date (YYYY-MM-DD); excludes unknown availability dates") LocalDate availableBefore
) {
    public void validateRanges() {
        if (minRent != null && maxRent != null && minRent > maxRent)
            throw invalid("minRent must be less than or equal to maxRent");
        if (minArea != null && maxArea != null && minArea > maxArea)
            throw invalid("minArea must be less than or equal to maxArea");
        if (minSharedHousingCapacity != null && !Boolean.TRUE.equals(sharedHousingAllowed))
            throw invalid("minSharedHousingCapacity requires sharedHousingAllowed=true");
    }

    private PropertyException invalid(String message) {
        return new PropertyException(400, "INVALID_PROPERTY_FILTER", message);
    }
}
