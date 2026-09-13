package com.habiterra.property.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "galerie_photo")
public class GaleriePhoto {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titre;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "galerie_photo_id", nullable = false)
    @org.hibernate.annotations.BatchSize(size = 50)
    @OrderBy("id ASC")
    private java.util.List<PhotoBien> photos = new java.util.ArrayList<>();

    public java.util.List<PhotoBien> getPhotos() {
        return photos;
    }

    public Long getId() {
        return id;
    }
    public String getTitre() {
        return titre;
    }
    public void setTitre(String value) {
        this.titre = value;
    }
}

