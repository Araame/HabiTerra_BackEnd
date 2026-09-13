package com.habiterra.property.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "photo_bien")
public class PhotoBien {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 1024)
    private String url;
    @Column(columnDefinition = "text")
    private String description;

    public Long getId() { return id; }
    public String getUrl() { return url; }
    public void setUrl(String value) { this.url = value; }
    public String getDescription() { return description; }
    public void setDescription(String value) { this.description = value; }
}

