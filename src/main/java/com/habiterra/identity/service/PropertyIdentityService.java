package com.habiterra.identity.service;

import com.habiterra.identity.entity.Proprietaire;
import com.habiterra.identity.repository.UtilisateurRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

/** Identity lookup exposed to other modules without exposing the identity repository. */
@Service
@Transactional(readOnly = true)
public class PropertyIdentityService {
    private final UtilisateurRepository users;
    public PropertyIdentityService(UtilisateurRepository users) { this.users = users; }
//Check if a user is a prop
    public Optional<Proprietaire> findOwner(Long id) {
        return users.findById(id).filter(Proprietaire.class::isInstance).map(Proprietaire.class::cast);
    }
}

