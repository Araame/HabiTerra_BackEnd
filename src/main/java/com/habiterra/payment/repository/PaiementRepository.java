package com.habiterra.payment.entity;

import com.habiterra.payment.entity.FournisseurPaiement;
import com.habiterra.payment.entity.StatutPaiement;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "paiement",
        indexes = {
                @Index(
                        name = "idx_paiement_echeance",
                        columnList = "echeance_id"
                ),
                @Index(
                        name = "idx_paiement_provider_token",
                        columnList = "provider_token"
                )
        }
)

// Payment model
public class Paiement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "echeance_id", nullable = false)
    private EcheanceLoyer echeance;

    @Column(
            name = "montant",
            nullable = false
    )
    private Long montant;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 20)
    private StatutPaiement statut;

    @Enumerated(EnumType.STRING)
    @Column(name = "fournisseur", nullable = false, length = 30)
    private FournisseurPaiement fournisseur;

    @Column(name = "provider_token", unique = true, length = 255)
    private String providerToken;

    @Column(name = "provider_reference", length = 255)
    private String providerReference;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;

    @Column(name = "date_paiement")
    private LocalDateTime datePaiement;

    protected Paiement() {
    }

    public Paiement(
            EcheanceLoyer echeance,
            Long montant,
            FournisseurPaiement fournisseur
    ) {
        this.echeance = echeance;
        this.montant = montant;
        this.fournisseur = fournisseur;
        this.statut = StatutPaiement.EN_ATTENTE;
        this.dateCreation = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public EcheanceLoyer getEcheance() {
        return echeance;
    }

    public Long getMontant() {
        return montant;
    }

    public StatutPaiement getStatut() {
        return statut;
    }

    public FournisseurPaiement getFournisseur() {
        return fournisseur;
    }

    public String getProviderToken() {
        return providerToken;
    }

    public String getProviderReference() {
        return providerReference;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public LocalDateTime getDatePaiement() {
        return datePaiement;
    }

    public void defineFurnisherInfos(
            String providerToken,
            String providerReference
    ) {
        this.providerToken = providerToken;
        this.providerReference = providerReference;
    }

    public void confirmer() {
        if (statut == StatutPaiement.PAYE) {
            return;
        }

        this.statut = StatutPaiement.PAYE;
        this.datePaiement = LocalDateTime.now();
    }

    public void echouer() {
        if (statut == StatutPaiement.EN_ATTENTE) {
            this.statut = StatutPaiement.ECHOUE;
        }
    }

    public void cancel() {
        if (statut == StatutPaiement.EN_ATTENTE) {
            this.statut = StatutPaiement.ANNULE;
        }
    }
}