package com.habiterra.identity.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "utilisateur")
@Inheritance(strategy = InheritanceType.JOINED)
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_utilisateur")
    private Long idUtilisateur;

    private String prenom;

    private String nom;

    @Column(unique = true, length = 100)
    private String email;

    @Column(unique = true, length = 16)
    private String telephone;

    @Column(name = "mot_de_passe")
    private String motDePasse;

    @Column(name = "photo_profil")
    private String photoProfil;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Enumerated(EnumType.STRING)
    private StatutUtilisateur statut;

    @Column(name = "email_verifie")
    private Boolean emailVerifie;

    @Column(name = "telephone_verifie")
    private Boolean telephoneVerifie;

    public Boolean getTelephoneVerifie() { return telephoneVerifie; }
    public void setTelephoneVerifie(Boolean telephoneVerifie) { this.telephoneVerifie = telephoneVerifie; }

    @Column(name = "date_creation")
    private LocalDateTime dateCreation;

    @Column(name = "derniere_connexion")
    private LocalDateTime derniereConnexion;

    public Utilisateur() {
    }

    public Long getIdUtilisateur() {
        return idUtilisateur;
    }

    public void setIdUtilisateur(Long idUtilisateur) {
        this.idUtilisateur = idUtilisateur;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    public String getPhotoProfil() {
        return photoProfil;
    }

    public void setPhotoProfil(String photoProfil) {
        this.photoProfil = photoProfil;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public StatutUtilisateur getStatut() {
        return statut;
    }

    public void setStatut(StatutUtilisateur statut) {
        this.statut = statut;
    }

    public Boolean getEmailVerifie() {
        return emailVerifie;
    }

    public void setEmailVerifie(Boolean emailVerifie) {
        this.emailVerifie = emailVerifie;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public LocalDateTime getDerniereConnexion() {
        return derniereConnexion;
    }

    public void setDerniereConnexion(LocalDateTime derniereConnexion) {
        this.derniereConnexion = derniereConnexion;
    }

    /** Operation du diagramme ; comportement a implementer. */
    public void seConnecter() {
        throw new UnsupportedOperationException("seConnecter : comportement non implemente");
    }

    /** Operation du diagramme ; comportement a implementer. */
    public void seDeconnecter() {
        throw new UnsupportedOperationException("seDeconnecter : comportement non implemente");
    }

    /** Operation du diagramme ; comportement a implementer. */
    public void modifierProfil() {
        throw new UnsupportedOperationException("modifierProfil : comportement non implemente");
    }

    /** Operation du diagramme ; comportement a implementer. */
    public void changerMotDePasse() {
        throw new UnsupportedOperationException("changerMotDePasse : comportement non implemente");
    }

    /** Operation du diagramme ; comportement a implementer. */
    public void verifierEmail() {
        throw new UnsupportedOperationException("verifierEmail : comportement non implemente");
    }

    /** Operation du diagramme ; comportement a implementer. */
    public void reinitialiserMotDePasse() {
        throw new UnsupportedOperationException("reinitialiserMotDePasse : comportement non implemente");
    }
}
