package com.habiterra.tenancy.entity;

import com.habiterra.identity.entity.Locataire;
import com.habiterra.property.entity.BienImmobilier;
import com.habiterra.tenancy.entity.StatutBail;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "bail")

// Bail model
public class Bail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "locataire_id", nullable = false)
    private Locataire locataire;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bien_immobilier_id", nullable = false)
    private BienImmobilier bienImmobilier;

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_fin")
    private LocalDate dateFin;

    @Column(
            name = "montant_loyer",
            nullable = false
    )
    private Long montantLoyer;

    @Column(
            name = "montant_caution",
            nullable = false
    )
    private Long montantCaution;

    @Column(name = "devise", nullable = false, length = 3)
    private String devise = "XOF";

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 30)
    private StatutBail statut = StatutBail.EN_PREPARATION;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;

    protected Bail() {
    }

    public Bail(
            Locataire locataire,
            BienImmobilier bienImmobilier,
            LocalDate dateDebut,
            LocalDate dateFin,
            Long montantLoyer,
            Long montantCaution,
            String devise
    ) {
        this.locataire = locataire;
        this.bienImmobilier = bienImmobilier;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.montantLoyer = montantLoyer;
        this.montantCaution = montantCaution;
        this.devise = devise;
        this.statut = StatutBail.EN_PREPARATION;
        this.dateCreation = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Locataire getLocataire() {
        return locataire;
    }

    public BienImmobilier getBienImmobilier() {
        return bienImmobilier;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public Long getMontantLoyer() {
        return montantLoyer;
    }

    public Long getMontantCaution() {
        return montantCaution;
    }

    public String getDevise() {
        return devise;
    }

    public StatutBail getStatut() {
        return statut;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void activer() {
        this.statut = StatutBail.ACTIF;
    }

    public void terminer() {
        this.statut = StatutBail.TERMINE;
    }

    public void resilier() {
        this.statut = StatutBail.RESILIE;
    }
}