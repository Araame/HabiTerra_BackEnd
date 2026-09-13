package com.habiterra.property.service;

import com.habiterra.identity.entity.*;
import com.habiterra.identity.service.AuthService;
import com.habiterra.identity.service.PropertyIdentityService;
import com.habiterra.property.entity.BienImmobilier;
import com.habiterra.property.exception.PropertyException;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.stereotype.Service;
import java.util.Objects;

@Service
public class PropertyAuthorizationService {
    private final AuthService authenticationService;
    private final PropertyIdentityService identities;

    public PropertyAuthorizationService(AuthService authenticationService, PropertyIdentityService identities) {
        this.authenticationService = authenticationService;
        this.identities = identities;
    }

    public Utilisateur currentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken)
            throw new PropertyException(401, "UNAUTHORIZED", "Authentication required");
        return authenticationService.activeUser(Long.valueOf(authentication.getName()));
    }

    public Utilisateur optionalUser(Authentication authentication) {
        return authentication == null || authentication instanceof AnonymousAuthenticationToken
                ? null : currentUser(authentication);
    }

    public void requireManager(Utilisateur user) {
        if (user.getRole() != Role.PROPRIETAIRE && user.getRole() != Role.GERANT_AGENCE)
            throw new PropertyException(403, "PROPERTY_ACCESS_DENIED", "Only owners and agency managers can manage properties");
    }

    public Long agencyId(Utilisateur user) {
        if (user instanceof GerantAgence manager && manager.getAgency() != null)
            return manager.getAgency().getId();
        throw new PropertyException(403, "AGENCY_REQUIRED", "Agency manager is not assigned to an agency");
    }

    public Proprietaire resolveOwner(Utilisateur user, Long ownerId) {
        requireManager(user);
        if (user.getRole() == Role.PROPRIETAIRE && user instanceof Proprietaire owner) {
            if (ownerId != null)
                throw new PropertyException(400, "OWNER_ID_NOT_ALLOWED", "Owners must not provide ownerId");
            return owner;
        }
        if (ownerId == null)
            throw new PropertyException(400, "OWNER_REQUIRED", "Agency creation requires ownerId");
        Proprietaire owner = identities.findOwner(ownerId).orElseThrow(() ->
                new PropertyException(404, "OWNER_NOT_FOUND", "Owner not found"));
        if (!canManageOwner(user, owner))
            throw new PropertyException(403, "OWNER_NOT_ALLOWED", "This agency is not authorized to manage the owner");
        return owner;
    }

    private boolean canManageOwner(Utilisateur user, Proprietaire owner) {
        if (user == null) return false;
        if (user.getRole() == Role.PROPRIETAIRE)
            return Objects.equals(owner.getIdUtilisateur(), user.getIdUtilisateur());
        if (user.getRole() == Role.GERANT_AGENCE && user instanceof GerantAgence manager)
            return manager.getAgency() != null && owner.getAgency() != null
                    && Objects.equals(manager.getAgency().getId(), owner.getAgency().getId());
        return false;
    }

    public boolean canManage(Utilisateur user, BienImmobilier property) {
        return canManageOwner(user, property.getOwner());
    }

    public void requireAccess(Utilisateur user, BienImmobilier property) {
        if (!canManage(user, property))
            throw new PropertyException(403, "PROPERTY_ACCESS_DENIED", "Property management is not allowed");
    }
}
