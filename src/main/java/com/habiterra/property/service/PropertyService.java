package com.habiterra.property.service;

import com.habiterra.identity.entity.*;
import com.habiterra.property.dto.*;
import com.habiterra.property.entity.*;
import com.habiterra.property.exception.PropertyException;
import com.habiterra.property.repository.*;
import com.habiterra.property.storage.PhotoStorageService;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;
import java.time.*;
import java.util.*;

@Service
@Validated
@Transactional(readOnly = true)
public class PropertyService {
    private final PropertyRepository properties;
    private final PropertyTypeRepository types;
    private final PropertyAuthorizationService authorization;
    private final PropertyMapper mapper;
    private final PhotoStorageService storage;
    private final Clock clock;
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PropertyService.class);
    //Avalaible sort fields
    private static final Set<String> SORT_FIELDS = Set.of("id", "dateCreation", "titre", "superficie",
            "montantLoyer", "montantCaution", "nombrePieces", "nombreChambres", "disponibleAPartirDu", "statut");

    public PropertyService(PropertyRepository properties, PropertyTypeRepository types,
            PropertyAuthorizationService authorization, PropertyMapper mapper, PhotoStorageService storage, Clock clock) {
        this.properties = properties;
        this.types = types;
        this.authorization = authorization;
        this.mapper = mapper;
        this.storage = storage;
        this.clock = clock;
    }


    //Create property
    @Transactional
    public PropertyResponse createProperty(@Valid CreatePropertyRequest request, Authentication authentication) {
        Utilisateur user = authorization.currentUser(authentication);
        Proprietaire owner = authorization.resolveOwner(user, request.ownerId());
        BienImmobilier property = new BienImmobilier(owner,
                user instanceof GerantAgence manager ? manager.getAgency() : null, LocalDateTime.now(clock));
        apply(property, new UpdatePropertyRequest(request.title(), request.description(), request.area(),
                request.numberOfRooms(), request.numberOfBedrooms(), request.numberOfBathrooms(),
                request.monthlyRent(), request.depositAmount(), request.furnished(), request.sharedHousingAllowed(),
                request.sharedHousingCapacity(), request.availableFrom(), request.typeId(), request.address(), request.rooms()));
        properties.saveAndFlush(property);
        return mapper.toResponse(property);
    }

    //Update property
    @Transactional
    public PropertyResponse updateProperty(Long id, @Valid UpdatePropertyRequest request, Authentication authentication) {
        BienImmobilier property = managedForUpdate(id, authentication);
        apply(property, request);
        //Synchronize the DB beacause the row already exists(already saved)
        properties.flush();
        return mapper.toResponse(property);
    }

    //Hnadle validation before creating or updating  a property
    private void apply(BienImmobilier property, UpdatePropertyRequest request) {
        //Number of bedrooms cannot exceed number of rooms
        if (request.numberOfBedrooms() > request.numberOfRooms())
            throw new PropertyException(400, "INVALID_PROPERTY", "Bedrooms must not exceed the number of rooms");

        //Coordinates must be valid
        if (request.address().latitude() != null && !Double.isFinite(request.address().latitude()) || request.address().longitude() != null && !Double.isFinite(request.address().longitude()))
            throw new PropertyException(400, "INVALID_ADDRESS", "Coordinates must be finite");

        //Property type must exist
        property.setType(types.findById(request.typeId()).orElseThrow(() -> new PropertyException(404, "PROPERTY_TYPE_NOT_FOUND", "Property type not found")));
        property.setTitre(request.title().strip());
        property.setDescription(request.description() == null ? null : request.description().strip());
        property.setSuperficie(request.area());
        property.setNombrePieces(request.numberOfRooms());
        property.setNombreChambres(request.numberOfBedrooms());
        property.setNombreSallesDeBain(request.numberOfBathrooms());
        property.setMontantLoyer(request.monthlyRent());
        property.setMontantCaution(request.depositAmount());
        property.setMeuble(request.furnished());
        property.setColocationAutorisee(request.sharedHousingAllowed());
        property.setCapaciteColocation(request.sharedHousingCapacity());
        property.setDisponibleAPartirDu(request.availableFrom());
        AddressRequest address = request.address();
        property.getAddress().setPays(address.country().strip());
        property.getAddress().setVille(address.city().strip());
        property.getAddress().setCommune(address.municipality());
        property.getAddress().setQuartier(address.neighborhood());
        property.getAddress().setRue(address.street());
        property.getAddress().setLatitude(address.latitude());
        property.getAddress().setLongitude(address.longitude());
        property.getRooms().clear();
        //Room area cannot exceed property area
        if (request.rooms() != null) for (RoomRequest room : request.rooms()) {
            if (room.area() > request.area())
                throw new PropertyException(400, "INVALID_ROOM", "Room area must not exceed property area");
            Piece piece = new Piece();
            piece.setNom(room.name().strip());
            piece.setSuperficie(room.area());
            piece.setDescription(room.description());
            property.getRooms().add(piece);
        }
        property.modifier();
    }

    //Retrieve connected user specific property
    public PropertyResponse getProperty(Long id, Authentication authentication) {
        return mapper.toResponse(readable(id, authentication));
    }

    //Retrieve readable properties
    private BienImmobilier readable(Long id, Authentication authentication) {
        BienImmobilier property = find(id);
        if (property.getStatut() != StatutBien.AVAILABLE && !authorization.canManage(authorization.optionalUser(authentication), property))
            throw notFound();
        return property;
    }

    //
    public Page<PropertyResponse> getPublicProperties(@Valid PropertyFilterRequest filters, Pageable pageable) {
        filters.validateRanges();
        return properties.findAll(PropertySpecifications.availableWithFilters(filters), pagination(pageable))
                .map(mapper::toResponse);
    }

    public Page<PropertyResponse> getManagedProperties(Authentication authentication, Pageable pageable) {
        Utilisateur user = authorization.currentUser(authentication);
        authorization.requireManager(user);
        Pageable page = pagination(pageable);
        return (user.getRole() == Role.PROPRIETAIRE
                ? properties.findByOwnerIdUtilisateur(user.getIdUtilisateur(), page)
                : properties.findByOwnerAgencyId(authorization.agencyId(user), page)).map(mapper::toResponse);
    }

    private Pageable pagination(Pageable pageable) {
        for (Sort.Order order : pageable.getSort())
            if (!SORT_FIELDS.contains(order.getProperty()))
                throw new PropertyException(400, "INVALID_SORT", "Unsupported property sort field");
        Sort sort = pageable.getSort().isSorted() ? pageable.getSort() : Sort.by(Sort.Direction.DESC, "dateCreation");
        if (sort.getOrderFor("id") == null) sort = sort.and(Sort.by("id"));
        return PageRequest.of(pageable.getPageNumber(), Math.min(pageable.getPageSize(), 100), sort);
    }


    //Publish a property
    @Transactional
    public PropertyResponse publishProperty(Long id, Authentication authentication) {
        BienImmobilier property = managedForUpdate(id, authentication);
        property.publier();
        return mapper.toResponse(property);
    }


    //Unpublish  a property
    @Transactional
    public PropertyResponse unpublishProperty(Long id, Authentication authentication) {
        BienImmobilier property = managedForUpdate(id, authentication);
        property.depublier();
        return mapper.toResponse(property);
    }

    //Add a photo for a property
    @Transactional
    public PhotoResponse addPhoto(Long id, MultipartFile file, String description, Authentication authentication) {
        BienImmobilier property = managedForUpdate(id, authentication);
        //Photo desc cannot be null or excedd 5000 char
        if (description != null && description.length() > 5000)
            throw new PropertyException(400, "INVALID_PHOTO_DESCRIPTION", "Photo description exceeds 5000 characters");
        //
        String key = storage.store(file);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED)
                    cleanup(key);
            }
        });
        PhotoBien photo = new PhotoBien();
        photo.setUrl("/api/v1/properties/" + id + "/photos/files/" + key);
        photo.setDescription(description);
        property.getGallery().getPhotos().add(photo);
        properties.flush();
        return mapper.toPhotoResponse(photo);
    }

    //Delete a property photo
    @Transactional
    public void deletePhoto(Long id, Long photoId, Authentication authentication) {
        BienImmobilier property = managedForUpdate(id, authentication);
        PhotoBien photo = property.getGallery().getPhotos().stream()
                .filter(candidate -> Objects.equals(candidate.getId(), photoId)).findFirst()
                .orElseThrow(() -> new PropertyException(404, "PHOTO_NOT_FOUND", "Photo not found on this property"));
        String key = photo.getUrl().substring(photo.getUrl().lastIndexOf('/') + 1);
        property.getGallery().getPhotos().remove(photo);
        properties.flush();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCommit() { cleanup(key); }
        });
    }

    private void cleanup(String key) {
        try { storage.delete(key); }
        catch (RuntimeException error) {
            log.error("Photo file cleanup failed for {}; manual cleanup required", key, error);
        }
    }

    public Resource getPhotoContent(Long id, String filename, Authentication authentication) {
        BienImmobilier property = readable(id, authentication);
        String url = "/api/v1/properties/" + id + "/photos/files/" + filename;
        if (property.getGallery().getPhotos().stream().noneMatch(photo -> url.equals(photo.getUrl())))
            throw new PropertyException(404, "PHOTO_NOT_FOUND", "Photo not found on this property");
        return storage.load(filename);
    }

    //Handle updating process ( who can update and what to update)
    private BienImmobilier managedForUpdate(Long id, Authentication authentication) {
        Utilisateur user = authorization.currentUser(authentication);
        BienImmobilier property = properties.findForUpdate(id).orElseThrow(this::notFound);
        authorization.requireAccess(user, property);
        return property;
    }
//Show specific property
    private BienImmobilier find(Long id) {
        return properties.findById(id).orElseThrow(this::notFound);
    }
    private PropertyException notFound() {
        return new PropertyException(404, "PROPERTY_NOT_FOUND", "Property not found");
    }
}
