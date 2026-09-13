package com.habiterra.property.repository;

import com.habiterra.property.dto.PropertyFilterRequest;
import com.habiterra.property.entity.BienImmobilier;
import com.habiterra.property.entity.StatutBien;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import java.util.ArrayList;
import java.util.List;

public final class PropertySpecifications {
    private PropertySpecifications() {}

    public static Specification<BienImmobilier> availableWithFilters(PropertyFilterRequest filters) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            // Public visibility is mandatory and cannot be supplied by the caller.
            predicates.add(cb.equal(root.get("statut"), StatutBien.AVAILABLE));
            if (hasText(filters.country()) || hasText(filters.city())
                    || hasText(filters.municipality()) || hasText(filters.neighborhood())) {
                var address = root.join("address");
                textEquals(predicates, cb, address.get("pays"), filters.country());
                textEquals(predicates, cb, address.get("ville"), filters.city());
                textEquals(predicates, cb, address.get("commune"), filters.municipality());
                textEquals(predicates, cb, address.get("quartier"), filters.neighborhood());
            }
            if (filters.typeId() != null)
                predicates.add(cb.equal(root.get("type").get("id"), filters.typeId()));
            if (filters.minRent() != null)
                predicates.add(cb.greaterThanOrEqualTo(root.get("montantLoyer"), filters.minRent()));
            if (filters.maxRent() != null)
                predicates.add(cb.lessThanOrEqualTo(root.get("montantLoyer"), filters.maxRent()));
            if (filters.rooms() != null)
                predicates.add(cb.equal(root.get("nombrePieces"), filters.rooms()));
            if (filters.bedrooms() != null)
                predicates.add(cb.equal(root.get("nombreChambres"), filters.bedrooms()));
            if (filters.bathrooms() != null)
                predicates.add(cb.equal(root.get("nombreSallesDeBain"), filters.bathrooms()));
            if (filters.minArea() != null)
                predicates.add(cb.greaterThanOrEqualTo(root.get("superficie"), filters.minArea()));
            if (filters.maxArea() != null)
                predicates.add(cb.lessThanOrEqualTo(root.get("superficie"), filters.maxArea()));
            if (filters.furnished() != null)
                predicates.add(cb.equal(root.get("meuble"), filters.furnished()));
            if (filters.sharedHousingAllowed() != null)
                predicates.add(cb.equal(root.get("colocationAutorisee"), filters.sharedHousingAllowed()));
            if (Boolean.TRUE.equals(filters.sharedHousingAllowed()) && filters.minSharedHousingCapacity() != null)
                predicates.add(cb.greaterThanOrEqualTo(root.get("capaciteColocation"), filters.minSharedHousingCapacity()));
            if (filters.availableBefore() != null)
                predicates.add(cb.lessThanOrEqualTo(root.get("disponibleAPartirDu"), filters.availableBefore()));
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static void textEquals(List<Predicate> predicates, CriteriaBuilder cb, Path<String> path, String value) {
        if (hasText(value))
            predicates.add(cb.equal(cb.lower(path), cb.lower(cb.literal(value.strip()))));
    }
}
