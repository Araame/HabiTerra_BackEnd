package com.habiterra.payment.repository;

import com.habiterra.payment.entity.EcheanceLoyer;
import com.habiterra.payment.entity.StatutEcheance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

// Echeance loyer repository
public interface EcheanceLoyerRepository
        extends JpaRepository<EcheanceLoyer, Long> {

    Optional<EcheanceLoyer> findByBailIdAndPeriode(
            Long bailId,
            String periode
    );

    boolean existsByBailIdAndPeriode(
            Long bailId,
            String periode
    );

    List<EcheanceLoyer> findByStatutAndDateEcheanceBefore(
            StatutEcheance statut,
            LocalDate date
    );
}