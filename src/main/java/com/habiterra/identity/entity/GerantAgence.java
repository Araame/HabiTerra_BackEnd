package com.habiterra.identity.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "gerant_agence")
@PrimaryKeyJoinColumn(name = "id_gerant")
public class GerantAgence extends Utilisateur {

    @Column(name = "poste")
    private String poste;



    public GerantAgence() {
    }

    public String getPoste() {
        return poste;
    }

    public void setPoste(String poste) {
        this.poste = poste;
    }



}
