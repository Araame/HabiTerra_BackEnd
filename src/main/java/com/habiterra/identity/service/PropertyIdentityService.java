package com.habiterra.identity.service;

import com.habiterra.identity.entity.Proprietaire;
import com.habiterra.identity.repository.ProprietaireRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

/** Identity lookup exposed to other modules without exposing the identity repository. */
@Service
@Transactional(readOnly = true)
public class PropertyIdentityService {
    private final ProprietaireRepository owners;
    public PropertyIdentityService(ProprietaireRepository owners) { this.owners = owners; }
//Check if a user is a prop
    public Optional<Proprietaire> findOwner(Long id) {
        return owners.findById(id);
    }

    public Optional<Proprietaire> findOwnerByUserId(Long userId) {
        return owners.findByUtilisateurIdUtilisateur(userId);
    }
}
