package com.habiterra.identity.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.math.BigDecimal;

@Entity
@Table(name = "locataire")
@PrimaryKeyJoinColumn(name = "id_locataire")
public class Locataire extends Utilisateur {


    private String profession;

    public Locataire() {
    }



    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }

}


