package com.habiterra.property.controller;

import com.habiterra.application.dto.ApplicationResponse;
import com.habiterra.application.service.ApplicationService;
import com.habiterra.property.dto.*;
import com.habiterra.property.service.PropertyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.net.URI;

@RestController
@RequestMapping("/api/v1/properties")
@Tag(name = "Properties", description = "Property creation, management, publication and photos")
public class PropertyController {
    private final PropertyService service;
    private final ApplicationService applications;
    public PropertyController(PropertyService service, ApplicationService applications) {
        this.service = service;
        this.applications = applications;
    }

    @GetMapping("/{propertyId}/applications")
    @Operation(summary = "List applications received for a managed property", description = "Owner or currently authorized agency only. Page size capped at 100. Sort fields: id, dateCandidature, statut. Default: dateCandidature DESC.")
    public Page<ApplicationResponse> getPropertyApplications(@PathVariable Long propertyId, Authentication authentication,
            @ParameterObject @PageableDefault(size = 20, sort = "dateCandidature", direction = Sort.Direction.DESC) Pageable pageable) {
        return applications.getPropertyApplications(propertyId, authentication, pageable);
    }

    @GetMapping
    @SecurityRequirements
    @Operation(summary = "List and filter available properties publicly", description = "Only AVAILABLE properties. Optional filters are combined with AND; ranges are inclusive. Location uses country, city, municipality and neighborhood (exact, case insensitive). Capacity requires sharedHousingAllowed=true. availableBefore excludes unknown dates. Existing pagination and sorting apply; page size is capped at 100. Example: city=Dakar&minRent=150000&bedrooms=2&sort=dateCreation,desc.")
    public Page<PropertyResponse> getPublicProperties(
            @Valid @ModelAttribute @ParameterObject PropertyFilterRequest filters,
            @ParameterObject @PageableDefault(size = 20, sort = "dateCreation", direction = Sort.Direction.DESC) Pageable pageable) {
        return service.getPublicProperties(filters, pageable);
    }

    @GetMapping("/mine")
    @Operation(summary = "List properties managed by the authenticated owner or agency", description = "Includes all four statuses. Agency access depends on the current owner assignment.")
    public Page<PropertyResponse> getManagedProperties(Authentication authentication,
            @ParameterObject @PageableDefault(size = 20, sort = "dateCreation", direction = Sort.Direction.DESC) Pageable pageable) {
        return service.getManagedProperties(authentication, pageable);
    }

    @GetMapping("/{id}")
    @SecurityRequirements
    @Operation(summary = "Get a property", description = "AVAILABLE is public. Other statuses require an authorized manager's JWT; otherwise returns 404.")
    public PropertyResponse getProperty(@PathVariable Long id, Authentication authentication) {
        return service.getProperty(id, authentication);
    }

    @PostMapping
    @Operation(summary = "Create a draft property", description = "PROPRIETAIRE must omit ownerId. GERANT_AGENCE must select a proprietor currently assigned to their agency.")
    public ResponseEntity<PropertyResponse> createProperty(@Valid @RequestBody CreatePropertyRequest request,
            Authentication authentication) {
        PropertyResponse response = service.createProperty(request, authentication);
        return ResponseEntity.created(URI.create("/api/v1/properties/" + response.id())).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Replace editable property information", description = "Owner, agency, creation date and status cannot be changed. Omitted rooms clears the room list. Published properties must remain publishable.")
    public PropertyResponse updateProperty(@PathVariable Long id, @Valid @RequestBody UpdatePropertyRequest request,
            Authentication authentication) {
        return service.updateProperty(id, request, authentication);
    }

    @PostMapping("/{id}/publish")
    @Operation(summary = "Publish a property", description = "DRAFT or UNAVAILABLE becomes AVAILABLE after validation. Description must contain at least 10 characters.")
    public PropertyResponse publishProperty(@PathVariable Long id, Authentication authentication) {
        return service.publishProperty(id, authentication);
    }

    @PostMapping("/{id}/unpublish")
    @Operation(summary = "Unpublish a property", description = "AVAILABLE becomes UNAVAILABLE. All property data and photos are retained.")
    public PropertyResponse unpublishProperty(@PathVariable Long id, Authentication authentication) {
        return service.unpublishProperty(id, authentication);
    }

    @PostMapping(value = "/{id}/photos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Add a photo", description = "Multipart file and optional description. JPEG/PNG up to 10 MiB and 25 megapixels by default. No primary photo.")
    public ResponseEntity<PhotoResponse> addPhoto(@PathVariable Long id, @RequestPart("file") MultipartFile file,
            @RequestParam(required = false) String description, Authentication authentication) {
        PhotoResponse response = service.addPhoto(id, file, description, authentication);
        return ResponseEntity.created(URI.create(response.url())).body(response);
    }

    @DeleteMapping("/{id}/photos/{photoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a photo belonging to a managed property")
    public void deletePhoto(@PathVariable Long id, @PathVariable Long photoId, Authentication authentication) {
        service.deletePhoto(id, photoId, authentication);
    }

    @GetMapping("/{id}/photos/files/{filename}")
    @SecurityRequirements
    @Operation(summary = "Read photo content", description = "Uses the same visibility rules as its property. Private photos require an authorized manager's JWT.")
    public ResponseEntity<Resource> getPhotoContent(@PathVariable Long id, @PathVariable String filename,
            Authentication authentication) {
        Resource resource = service.getPhotoContent(id, filename, authentication);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .header("X-Content-Type-Options", "nosniff")
                .contentType(filename.endsWith(".png") ? MediaType.IMAGE_PNG : MediaType.IMAGE_JPEG)
                .body(resource);
    }
}
