package com.habiterra.property.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "type_bien")
public class TypeBien {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String libelle;
    @Column(columnDefinition = "text")
    private String description;

    public Long getId() { return id; }
    public String getLibelle() { return libelle; }
    public void setLibelle(String value) { this.libelle = value; }
    public String getDescription() { return description; }
    public void setDescription(String value) { this.description = value; }
}

