package com.habiterra.agency.entity;

import jakarta.persistence.*;

/** Minimal agency identity; agency administration is outside the property module. */
@Entity
@Table(name = "agence")
public class Agence {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    protected Agence() {}
    public Long getId() { return id; }
}

