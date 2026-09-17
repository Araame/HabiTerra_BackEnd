package com.habiterra.application.controller;

import com.habiterra.application.dto.*;
import com.habiterra.application.service.ApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.net.URI;

// Application controller
@RestController
@RequestMapping("/api/v1/applications")
@Tag(name = "Applications", description = "Tenant applications, consultation and decisions")
public class ApplicationController {
    private final ApplicationService service;

    public ApplicationController(ApplicationService service) { this.service = service; }

    // Submitting application endpoint
    @PostMapping
    @Operation(summary = "Apply for an available property", description = "Tenants only. Send propertyId only. Status starts at EN_ATTENTE. One application per tenant and property, including terminal applications.")
    public ResponseEntity<ApplicationResponse> submitApplication(@Valid @RequestBody CreateApplicationRequest request,
            Authentication authentication) {
        ApplicationResponse response = service.submitApplication(request, authentication);
        return ResponseEntity.created(URI.create("/api/v1/applications/" + response.id())).body(response);
    }

    // Getting connected user applications
    @GetMapping("/mine")
    @Operation(summary = "List the authenticated tenant's applications", description = "Page size capped at 100. Sort fields: id, dateCandidature, statut. Default: dateCandidature DESC.")
    public Page<ApplicationResponse> getMyApplications(Authentication authentication,
            @ParameterObject @PageableDefault(size = 20, sort = "dateCandidature", direction = Sort.Direction.DESC) Pageable pageable) {
        return service.getMyApplications(authentication, pageable);
    }

    // Getting a specific application
    @GetMapping("/{id}")
    @Operation(summary = "Get an application", description = "Only the applicant or an authorized property owner or agency manager.")
    public ApplicationResponse getApplication(@PathVariable Long id, Authentication authentication) {
        return service.getApplication(id, authentication);
    }

    // Mark in review a specific application endpoint
    @PostMapping("/{id}/review")
    @Operation(summary = "Review an application", description = "Authorized property managers only. EN_ATTENTE becomes EN_ETUDE.")
    public ApplicationResponse markUnderReview(@PathVariable Long id, Authentication authentication) {
        return service.markUnderReview(id, authentication);
    }

    // Accepting a specific application endpoint
    @PostMapping("/{id}/accept")
    @Operation(summary = "Accept an application", description = "Authorized property managers only. EN_ETUDE becomes ACCEPTEE. Selects the candidate only; does not create a tenancy or change property status.")
    public ApplicationResponse acceptApplication(@PathVariable Long id, Authentication authentication) {
        return service.acceptApplication(id, authentication);
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "Reject an application", description = "Authorized property managers only. EN_ETUDE becomes REJETEE.")
    public ApplicationResponse rejectApplication(@PathVariable Long id, Authentication authentication) {
        return service.rejectApplication(id, authentication);
    }

    // Cancellling a specific application
    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel an application", description = "Applicant only. EN_ATTENTE or EN_ETUDE becomes ANNULEE. Terminal statuses cannot change.")
    public ApplicationResponse cancelApplication(@PathVariable Long id, Authentication authentication) {
        return service.cancelApplication(id, authentication);
    }
}
