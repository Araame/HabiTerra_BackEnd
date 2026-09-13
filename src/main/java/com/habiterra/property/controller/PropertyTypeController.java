package com.habiterra.property.controller;

import com.habiterra.property.dto.CreatePropertyTypeRequest;
import com.habiterra.property.dto.PropertyTypeResponse;
import com.habiterra.property.service.PropertyTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/property-types")
@Tag(name = "Property types", description = "Property type catalog")
public class PropertyTypeController {
    private final PropertyTypeService service;

    public PropertyTypeController(PropertyTypeService service) { this.service = service; }

    @GetMapping
    @SecurityRequirements
    @Operation(summary = "List property types", description = "Sorted by label, then id.")
    public List<PropertyTypeResponse> list() { return service.list(); }

    @GetMapping("/{id}")
    @SecurityRequirements
    @Operation(summary = "Get a property type")
    public PropertyTypeResponse get(@PathVariable Long id) { return service.get(id); }

    @PostMapping
    @Operation(summary = "Create a property type", description = "Requires an active PROPRIETAIRE or GERANT_AGENCE account. The returned id can be used as typeId when creating a property.")
    public ResponseEntity<PropertyTypeResponse> create(@Valid @RequestBody CreatePropertyTypeRequest request,
            Authentication authentication) {
        PropertyTypeResponse response = service.create(request, authentication);
        return ResponseEntity.created(URI.create("/api/v1/property-types/" + response.id())).body(response);
    }
}
