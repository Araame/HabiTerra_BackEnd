package com.habiterra.payment.service;

import com.habiterra.payment.entity.EcheanceLoyer;
import com.habiterra.payment.repository.EcheanceLoyerRepository;
import com.habiterra.tenancy.entity.Bail;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;

@Service
public class EcheanceLoyerService {

    private final EcheanceLoyerRepository repository;

    public EcheanceLoyerService(
            EcheanceLoyerRepository repository
    ) {
        this.repository = repository;
    }


    // Generate an echeance for a specific month
    @Transactional
    public EcheanceLoyer genererPourMois(
            Bail bail,
            YearMonth periode
    ) {
        String periodeValue = periode.toString();

        return repository
                .findByBailIdAndPeriode(
                        bail.getId(),
                        periodeValue
                )
                .orElseGet(() -> {
                    LocalDate dateEcheance =
                            periode.atDay(bail.getJourEcheance());

                    EcheanceLoyer echeance =
                            new EcheanceLoyer(
                                    bail,
                                    periode,
                                    bail.getMontantLoyer(),
                                    dateEcheance
                            );

                    return repository.save(echeance);
                });
    }
}