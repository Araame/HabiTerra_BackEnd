package com.habiterra.identity.repository;

import com.habiterra.identity.entity.Locataire;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface LocataireRepository extends JpaRepository<Locataire, Long> {
    Optional<Locataire> findByUtilisateurIdUtilisateur(Long userId);
}
