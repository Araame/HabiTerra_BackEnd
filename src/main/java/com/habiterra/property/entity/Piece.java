package com.habiterra.property.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "piece")
public class Piece {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;

    private Integer superficie;
    @Column(columnDefinition = "text")
    private String description;

    public Long getId() { return id; }
    public String getNom() { return nom; }
    public void setNom(String value) { this.nom = value; }
    public Integer getSuperficie() { return superficie; }
    public void setSuperficie(Integer value) { this.superficie = value; }
    public String getDescription() { return description; }
    public void setDescription(String value) { this.description = value; }
}

