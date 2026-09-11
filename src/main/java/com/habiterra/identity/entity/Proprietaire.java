package com.habiterra.identity.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "proprietaire")
@PrimaryKeyJoinColumn(name = "id_proprietaire")
public class Proprietaire extends Utilisateur {

    @Column(name = "numero_identite")
    private String numeroIdentite;

    @Column(name = "adresse_residence")
    private String adresseResidence;

    private String profession;



    public Proprietaire() {
    }

    public String getNumeroIdentite() {
        return numeroIdentite;
    }

    public void setNumeroIdentite(String numeroIdentite) {
        this.numeroIdentite = numeroIdentite;
    }

    public String getAdresseResidence() {
        return adresseResidence;
    }

    public void setAdresseResidence(String adresseResidence) {
        this.adresseResidence = adresseResidence;
    }

    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }


}

