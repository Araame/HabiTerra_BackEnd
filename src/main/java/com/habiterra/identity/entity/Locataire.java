package com.habiterra.identity.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.math.BigDecimal;

@Entity
@Table(name = "locataire")
public class Locataire {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_locataire")
    private Long id;

    @Column(nullable = false, length = 100)
    private String prenom;
    @Column(nullable = false, length = 100)
    private String nom;
    @Column(length = 100)
    private String email;
    @Column(length = 16)
    private String telephone;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", unique = true)
    private Utilisateur utilisateur;

    public Long getId() { return id; }
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    public Utilisateur getUtilisateur() { return utilisateur; }

    /** Linking an account never replaces the business identity or management assignment. */
    public void setUtilisateur(Utilisateur utilisateur) {
        if (utilisateur != null && utilisateur.getRole() != Role.LOCATAIRE)
            throw new com.habiterra.identity.exception.AuthException(400, "PROFILE_ROLE_INCOMPATIBLE", "Role incompatible avec le profil");
        if (this.utilisateur != null && (utilisateur == null ||
                (this.utilisateur != utilisateur && (this.utilisateur.getIdUtilisateur() == null ||
                !this.utilisateur.getIdUtilisateur().equals(utilisateur.getIdUtilisateur())))))
            throw new com.habiterra.identity.exception.AuthException(409, "PROFILE_ALREADY_ASSOCIATED", "Profil deja associe");
        this.utilisateur = utilisateur;
    }

    @PrePersist
    @PreUpdate
    private void validateAccountRole() {
        if (utilisateur != null && utilisateur.getRole() != Role.LOCATAIRE)
            throw new com.habiterra.identity.exception.AuthException(400, "PROFILE_ROLE_INCOMPATIBLE", "Role incompatible avec le profil");
    }


    private String profession;

    public Locataire() {
    }



    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }

}

