package com.habiterra.payment.entity;

import com.habiterra.payment.entity.StatutEcheance;
import com.habiterra.tenancy.entity.Bail;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

@Entity
@Table(name = "echeance_loyer",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_echeance_bail_periode",
                        columnNames = {"bail_id", "periode"}
                )
        }
)

// Echeance Loyer model
public class EcheanceLoyer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bail_id", nullable = false)
    private Bail bail;

    @Column(name = "periode", nullable = false, length = 7)
    private String periode;

    @Column(
            name = "montant",
            nullable = false
    )
    private Long montant;

    @Column(name = "date_echeance", nullable = false)
    private LocalDate dateEcheance;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 20)
    private StatutEcheance statut;

    protected EcheanceLoyer() {
    }

    public EcheanceLoyer(
            Bail bail,
            YearMonth periode,
            Long montant,
            LocalDate dateEcheance
    ) {
        this.bail = bail;
        this.periode = periode.toString();
        this.montant = montant;
        this.dateEcheance = dateEcheance;
        this.statut = StatutEcheance.A_PAYER;
    }

    public Long getId() {
        return id;
    }

    public Bail getBail() {
        return bail;
    }

    public YearMonth getPeriode() {
        return YearMonth.parse(periode);
    }

    public Long getMontant() {
        return montant;
    }

    public LocalDate getDateEcheance() {
        return dateEcheance;
    }

    public StatutEcheance getStatut() {
        return statut;
    }

    public void marquerEnRetard() {
        if (statut == StatutEcheance.A_PAYER) {
            statut = StatutEcheance.EN_RETARD;
        }
    }

    public void marquerPayee() {
        statut = StatutEcheance.PAYEE;
    }
}