package com.habiterra.property.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "adresse")
public class Adresse {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String pays;

    private String ville;

    private String commune;

    private String quartier;

    private String rue;

    private Double latitude;

    private Double longitude;

    public Long getId() {
        return id;
    }
    public String getPays() {
        return pays;
    }
    public void setPays(String value) {
        this.pays = value;
    }
    public String getVille() {
        return ville;
    }
    public void setVille(String value) {
        this.ville = value;
    }
    public String getCommune() {
        return commune;
    }
    public void setCommune(String value) {
        this.commune = value;
    }
    public String getQuartier() {
        return quartier;
    }
    public void setQuartier(String value) {
        this.quartier = value;
    }
    public String getRue() {
        return rue;
    }
    public void setRue(String value) {
        this.rue = value;
    }
    public Double getLatitude() {
        return latitude;
    }
    public void setLatitude(Double value) {
        this.latitude = value;
    }
    public Double getLongitude() {
        return longitude;
    }
    public void setLongitude(Double value) {
        this.longitude = value;
    }
}

