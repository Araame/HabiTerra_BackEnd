package com.habiterra.identity.repository;

import com.habiterra.identity.entity.Proprietaire;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ProprietaireRepository extends JpaRepository<Proprietaire, Long> {
    Optional<Proprietaire> findByUtilisateurIdUtilisateur(Long userId);
}
