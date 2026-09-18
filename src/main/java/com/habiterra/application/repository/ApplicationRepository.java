package com.habiterra.application.repository;

import com.habiterra.application.entity.Candidature;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

// Application repositrory
public interface ApplicationRepository extends JpaRepository<Candidature, Long> {
    boolean existsByLocataireIdAndBienImmobilierId(Long tenantId, Long propertyId);

    @EntityGraph(attributePaths = {"locataire", "bienImmobilier"})
    Page<Candidature> findByLocataireId(Long tenantId, Pageable pageable);

    @EntityGraph(attributePaths = {"locataire", "bienImmobilier"})
    Page<Candidature> findByBienImmobilierId(Long propertyId, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"locataire", "bienImmobilier"})
    Optional<Candidature> findById(Long id);

// Lock the concerned line until reading process is over (avoid writing and reading simultaneously)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Candidature c where c.id = :id")
    Optional<Candidature> findForUpdate(@Param("id") Long id);
}
