package com.habiterra.identity.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "gerant_agence")
@PrimaryKeyJoinColumn(name = "id_gerant")
public class GerantAgence extends Utilisateur {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agency_id")
    private com.habiterra.agency.entity.Agence agency;

    public com.habiterra.agency.entity.Agence getAgency() { return agency; }
    public void setAgency(com.habiterra.agency.entity.Agence agency) { this.agency = agency; }

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
