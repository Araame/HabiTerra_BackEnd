package com.habiterra.identity.dto;
import jakarta.validation.constraints.*;
import com.habiterra.identity.entity.Role;
public record UserResponse(Long id, String prenom, String nom, String email, String telephone, Role role, String photoProfil, String profession, String poste) { }
