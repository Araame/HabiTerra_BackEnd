package com.habiterra.property.entity;

import com.habiterra.agency.entity.Agence;
import com.habiterra.identity.entity.Proprietaire;
import com.habiterra.property.exception.PropertyException;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bien_immobilier")
public class BienImmobilier {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titre;
    @Column(columnDefinition = "text")
    private String description;

    private Integer superficie;

    private Integer nombrePieces;

    private Integer nombreChambres;

    private Integer nombreSallesDeBain;

    private Integer montantLoyer;

    private Integer montantCaution;

    private Boolean meuble;

    private Boolean colocationAutorisee;

    private Integer capaciteColocation;

    @Column(name = "disponible_a_partir_du")
    private LocalDate disponibleAPartirDu;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutBien statut = StatutBien.DRAFT;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false, updatable = false)
    private Proprietaire owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agency_id", updatable = false)
    private Agence agency;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "type_bien_id", nullable = false)
    private TypeBien type;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, optional = false, orphanRemoval = true)
    @JoinColumn(name = "adresse_id", nullable = false, unique = true)
    private Adresse address;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, optional = false, orphanRemoval = true)
    @JoinColumn(name = "galerie_photo_id", nullable = false, unique = true)
    private GaleriePhoto gallery;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "bien_immobilier_id", nullable = false)

    //Optimize requests into the BD
    @org.hibernate.annotations.BatchSize(size = 50)
    @OrderBy("id ASC")


    private List<Piece> rooms = new ArrayList<>();

    protected BienImmobilier() {}
    public BienImmobilier(Proprietaire owner, Agence agency, LocalDateTime creationDate) {
        this.owner = java.util.Objects.requireNonNull(owner);
        this.agency = agency;
        this.dateCreation = java.util.Objects.requireNonNull(creationDate);
        this.address = new Adresse();
        this.gallery = new GaleriePhoto();
    }

    public void modifier() {
        if (Boolean.TRUE.equals(colocationAutorisee)) {
            if (capaciteColocation == null || capaciteColocation <= 0)
                throw new PropertyException(400, "INVALID_SHARED_HOUSING", "Shared housing requires a positive maximum capacity");
        } else {
            capaciteColocation = null;
        }
        if (statut == StatutBien.AVAILABLE) validatePublication();
    }

    public void publier() {
        if (statut != StatutBien.DRAFT && statut != StatutBien.UNAVAILABLE)
            throw new PropertyException(409, "INVALID_PUBLICATION_TRANSITION", "Only draft or unavailable properties can be published");
        validatePublication();
        statut = StatutBien.AVAILABLE;
    }

    public void depublier() {
        if (statut != StatutBien.AVAILABLE)
            throw new PropertyException(409, "INVALID_UNPUBLICATION_TRANSITION", "Only available properties can be unpublished");
        statut = StatutBien.UNAVAILABLE;
    }

    private void validatePublication() {
        if (titre == null || titre.isBlank() || description == null || description.strip().length() < 10
                || superficie == null || superficie <= 0
                || montantLoyer == null || montantLoyer <= 0
                || montantCaution == null || montantCaution < 0
                || type == null || address == null || owner == null
                || address.getPays() == null || address.getPays().isBlank()
                || address.getVille() == null || address.getVille().isBlank()
                || nombrePieces == null || nombrePieces <= 0
                || nombreChambres == null || nombreChambres < 0
                || nombreSallesDeBain == null || nombreSallesDeBain < 0
                || nombreChambres > nombrePieces || meuble == null || colocationAutorisee == null
                || (Boolean.TRUE.equals(colocationAutorisee) && (capaciteColocation == null || capaciteColocation <= 0))
                || (!Boolean.TRUE.equals(colocationAutorisee) && capaciteColocation != null)) {
            throw new PropertyException(400, "PROPERTY_NOT_PUBLISHABLE", "Property information is incomplete or invalid for publication");
        }
    }

    public Long getId() {
        return id;
    }
    public StatutBien getStatut() {
        return statut;
    }
    public LocalDateTime getDateCreation() {
        return dateCreation;
    }
    public Proprietaire getOwner() {
        return owner;
    }
    public Agence getAgency() {
        return agency;
    }
    public TypeBien getType() {
        return type;
    }
    public void setType(TypeBien value) {
        type = value;
    }
    public Adresse getAddress() {
        return address;
    }
    public GaleriePhoto getGallery() {
        return gallery;
    }
    public List<Piece> getRooms() {
        return rooms;
    }
    public String getTitre() {
        return titre;
    }
    public void setTitre(String value) {
        this.titre = value;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String value) {
        this.description = value;
    }
    public Integer getSuperficie() {
        return superficie;
    }
    public void setSuperficie(Integer value) {
        this.superficie = value;
    }
    public Integer getNombrePieces() {
        return nombrePieces;
    }
    public void setNombrePieces(Integer value) {
        this.nombrePieces = value;
    }
    public Integer getNombreChambres() {
        return nombreChambres;
    }
    public void setNombreChambres(Integer value) {
        this.nombreChambres = value;
    }
    public Integer getNombreSallesDeBain() {
        return nombreSallesDeBain;
    }
    public void setNombreSallesDeBain(Integer value) {
        this.nombreSallesDeBain = value;
    }
    public Integer getMontantLoyer() {
        return montantLoyer;
    }
    public void setMontantLoyer(Integer value) {
        this.montantLoyer = value;
    }
    public Integer getMontantCaution() {
        return montantCaution;
    }
    public void setMontantCaution(Integer value) {
        this.montantCaution = value;
    }
    public Boolean getMeuble() {
        return meuble;
    }
    public void setMeuble(Boolean value) {
        this.meuble = value;
    }
    public Boolean getColocationAutorisee() {
        return colocationAutorisee;
    }
    public void setColocationAutorisee(Boolean value) {
        this.colocationAutorisee = value;
    }
    public Integer getCapaciteColocation() {
        return capaciteColocation;
    }
    public void setCapaciteColocation(Integer value) {
        this.capaciteColocation = value;
    }
    public LocalDate getDisponibleAPartirDu() {
        return disponibleAPartirDu;
    }
    public void setDisponibleAPartirDu(LocalDate value) {
        this.disponibleAPartirDu = value;
    }
}
