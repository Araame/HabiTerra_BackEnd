package com.habiterra.application.entity;

import com.habiterra.application.exception.ApplicationException;
import com.habiterra.identity.entity.Locataire;
import com.habiterra.property.entity.BienImmobilier;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

// Candidature entity
@Entity
@Table(name = "candidature", uniqueConstraints = @UniqueConstraint(
        name = "uk_candidature_locataire_bien", columnNames = {"locataire_id", "bien_immobilier_id"}))

public class Candidature {
    protected Candidature() {}
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutCandidature statut = StatutCandidature.EN_ATTENTE;

    @Column(name = "date_candidature", nullable = false, updatable = false)
    private LocalDateTime dateCandidature;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "locataire_id", nullable = false, updatable = false)
    private Locataire locataire;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bien_immobilier_id", nullable = false, updatable = false)
    private BienImmobilier bienImmobilier;


    public Candidature(Locataire locataire, BienImmobilier bienImmobilier, LocalDateTime dateCandidature) {
        this.locataire = Objects.requireNonNull(locataire);
        this.bienImmobilier = Objects.requireNonNull(bienImmobilier);
        this.dateCandidature = Objects.requireNonNull(dateCandidature);
    }

    //Mark an application that it is in review
    public void markUnderReview() {
        requireStatus(StatutCandidature.EN_ATTENTE);
        statut = StatutCandidature.EN_ETUDE;
    }
//Accepting an application
    public void accept() {
        requireStatus(StatutCandidature.EN_ETUDE);
        statut = StatutCandidature.ACCEPTEE;
    }

    //Rejecting an application
    public void reject() {
        requireStatus(StatutCandidature.EN_ETUDE);
        statut = StatutCandidature.REJETEE;
    }

    //Cannot cancel application with status = pending or in review
    public void cancel() {
        if (statut != StatutCandidature.EN_ATTENTE && statut != StatutCandidature.EN_ETUDE)
            throw new ApplicationException(409, "INVALID_APPLICATION_STATUS", "Only pending or under-review applications can be cancelled");
        statut = StatutCandidature.ANNULEE;
    }
//Handle require status before attempting an action (cancelling...)
    private void requireStatus(StatutCandidature expected) {
        if (statut != expected)
            throw new ApplicationException(409, "INVALID_APPLICATION_STATUS", "Application status must be " + expected);
    }

    public Long getId() {
        return id;
    }
    public StatutCandidature getStatut() {
        return statut;
    }
    public LocalDateTime getDateCandidature() {
        return dateCandidature;
    }
    public Locataire getLocataire() {
        return locataire;
    }
    public BienImmobilier getBienImmobilier() {
        return bienImmobilier;
    }
}
