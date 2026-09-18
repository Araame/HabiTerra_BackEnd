package com.habiterra.application.service;

import com.habiterra.application.dto.*;
import com.habiterra.application.entity.Candidature;
import com.habiterra.application.exception.ApplicationException;
import com.habiterra.application.repository.ApplicationRepository;
import com.habiterra.identity.entity.*;
import com.habiterra.property.entity.*;
import com.habiterra.property.exception.PropertyException;
import com.habiterra.property.repository.PropertyRepository;
import com.habiterra.property.service.PropertyAuthorizationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import java.time.*;
import java.util.*;

//Application service
@Service
@Validated
@Transactional(readOnly = true)
public class ApplicationService {
    private static final Set<String> SORT_FIELDS = Set.of("id", "dateCandidature", "statut");
    private final ApplicationRepository applications;
    private final PropertyRepository properties;
    private final PropertyAuthorizationService authorization;
    private final Clock clock;
    private final com.habiterra.identity.repository.LocataireRepository tenants;

    public ApplicationService(ApplicationRepository applications, PropertyRepository properties,
            PropertyAuthorizationService authorization, Clock clock,
            com.habiterra.identity.repository.LocataireRepository tenants) {
        this.tenants = tenants;
        this.applications = applications;
        this.properties = properties;
        this.authorization = authorization;
        this.clock = clock;
    }

    // Submit an application
    @Transactional
    public ApplicationResponse submitApplication(@Valid CreateApplicationRequest request, Authentication authentication) {
        Locataire tenant = currentTenant(authentication);
        // Use the same lock as property publication to keep availability stable until insertion.
        BienImmobilier property = properties.findForUpdate(request.propertyId()).orElseThrow(this::propertyNotFound);

        if (applications.existsByLocataireIdAndBienImmobilierId(tenant.getId(), property.getId()))
            throw new ApplicationException(409, "DUPLICATE_APPLICATION", "You have already applied for this property");
        if (property.getStatut() != StatutBien.AVAILABLE)
            throw new ApplicationException(409, "PROPERTY_NOT_AVAILABLE", "Only available properties accept applications");
        Candidature application = new Candidature(tenant, property, LocalDateTime.now(clock));
        applications.saveAndFlush(application);
        return toResponse(application);
    }

    // Getting connected user applications pages
    public Page<ApplicationResponse> getMyApplications(Authentication authentication, Pageable pageable) {
        Locataire tenant = currentTenant(authentication);
        return applications.findByLocataireId(tenant.getId(), pagination(pageable))
                .map(this::toResponse);
    }

    // Getting connected user applications
    public ApplicationResponse getApplication(Long id, Authentication authentication) {
        Utilisateur user = authorization.currentUser(authentication);
        Candidature application = applications.findById(id).orElseThrow(this::notFound);

        if (!isApplicant(user, application) && !authorization.canManage(user, application.getBienImmobilier()))
            throw accessDenied();
        return toResponse(application);
    }

    // Getting applications for a specific property
    public Page<ApplicationResponse> getPropertyApplications(Long propertyId, Authentication authentication, Pageable pageable) {
        Utilisateur user = authorization.currentUser(authentication);
        authorization.requireManager(user);
        BienImmobilier property = properties.findById(propertyId).orElseThrow(this::propertyNotFound);
        authorization.requireAccess(user, property);
        return applications.findByBienImmobilierId(propertyId, pagination(pageable)).map(this::toResponse);
    }

    // Mark an application in review
    @Transactional
    public ApplicationResponse markUnderReview(Long id, Authentication authentication) {
        Candidature application = managedForUpdate(id, authentication);
        application.markUnderReview();
        return toResponse(application);
    }

    // Accepting application
    @Transactional
    public ApplicationResponse acceptApplication(Long id, Authentication authentication) {
        Candidature application = managedForUpdate(id, authentication);
        application.accept();
        return toResponse(application);
    }

    // Reject application
    @Transactional
    public ApplicationResponse rejectApplication(Long id, Authentication authentication) {
        Candidature application = managedForUpdate(id, authentication);
        application.reject();
        return toResponse(application);
    }

    // Cancelling application
    @Transactional
    public ApplicationResponse cancelApplication(Long id, Authentication authentication) {
        Locataire tenant = currentTenant(authentication);
        Candidature application = applications.findForUpdate(id).orElseThrow(this::notFound);
        if (!Objects.equals(tenant.getId(), application.getLocataire().getId())) throw accessDenied();
        application.cancel();
        return toResponse(application);
    }

    private Candidature managedForUpdate(Long id, Authentication authentication) {
        Utilisateur user = authorization.currentUser(authentication);
        authorization.requireManager(user);
        Candidature application = applications.findForUpdate(id).orElseThrow(this::notFound);
        authorization.requireAccess(user, application.getBienImmobilier());
        return application;
    }

    // Getting connected user
    private Locataire currentTenant(Authentication authentication) {
        Utilisateur user = authorization.currentUser(authentication);
        if (user.getRole() != Role.LOCATAIRE)
            throw new ApplicationException(403, "APPLICATION_ACCESS_DENIED", "Only tenants can perform this action");
        return tenants.findByUtilisateurIdUtilisateur(user.getIdUtilisateur()).orElseThrow(() ->
                new ApplicationException(403, "TENANT_PROFILE_NOT_FOUND", "Tenant profile unavailable"));
    }

    private boolean isApplicant(Utilisateur user, Candidature application) {
        return user.getRole() == Role.LOCATAIRE && application.getLocataire().getUtilisateur() != null
                && Objects.equals(user.getIdUtilisateur(), application.getLocataire().getUtilisateur().getIdUtilisateur());
    }

    // Personnalized pagination for application
    private Pageable pagination(Pageable pageable) {
        for (Sort.Order order : pageable.getSort())
            if (!SORT_FIELDS.contains(order.getProperty()))
                throw new ApplicationException(400, "INVALID_SORT", "Unsupported application sort field");
        Sort sort = pageable.getSort().isSorted() ? pageable.getSort() : Sort.by(Sort.Direction.DESC, "dateCandidature");
        if (sort.getOrderFor("id") == null) sort = sort.and(Sort.by("id"));
        return PageRequest.of(pageable.getPageNumber(), Math.min(pageable.getPageSize(), 100), sort);
    }

    private ApplicationResponse toResponse(Candidature application) {
        BienImmobilier property = application.getBienImmobilier();
        Locataire tenant = application.getLocataire();
        return new ApplicationResponse(application.getId(), application.getStatut(), application.getDateCandidature(),
                new PropertySummaryResponse(property.getId(), property.getTitre()),
                new TenantSummaryResponse(tenant.getId(), tenant.getPrenom(), tenant.getNom()));
    }

    // Application not found exception
    private ApplicationException notFound() {
        return new ApplicationException(404, "APPLICATION_NOT_FOUND", "Application not found");
    }

    // Application inaccessible exception
    private ApplicationException accessDenied() {
        return new ApplicationException(403, "APPLICATION_ACCESS_DENIED", "Application access is not allowed");
    }
// Property not found exception
    private PropertyException propertyNotFound() {
        return new PropertyException(404, "PROPERTY_NOT_FOUND", "Property not found");
    }
}
