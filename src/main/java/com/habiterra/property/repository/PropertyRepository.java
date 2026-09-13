package com.habiterra.property.repository;

import com.habiterra.property.entity.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface PropertyRepository extends JpaRepository<BienImmobilier, Long>, JpaSpecificationExecutor<BienImmobilier> {
    @Override
    @EntityGraph(attributePaths = {"owner", "agency", "type", "address", "gallery"})
    Page<BienImmobilier> findAll(org.springframework.data.jpa.domain.Specification<BienImmobilier> specification, Pageable pageable);

    @EntityGraph(attributePaths = {"owner", "agency", "type", "address", "gallery"})
    Page<BienImmobilier> findByOwnerIdUtilisateur(Long ownerId, Pageable pageable);

    @EntityGraph(attributePaths = {"owner", "owner.agency", "agency", "type", "address", "gallery"})
    Page<BienImmobilier> findByOwnerAgencyId(Long agencyId, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"owner", "agency", "type", "address", "gallery"})
    Optional<BienImmobilier> findById(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from BienImmobilier p where p.id = :id")
    Optional<BienImmobilier> findForUpdate(@Param("id") Long id);
}
