package com.habiterra.property.service;

import com.habiterra.property.dto.CreatePropertyTypeRequest;
import com.habiterra.property.dto.PropertyTypeResponse;
import com.habiterra.property.entity.TypeBien;
import com.habiterra.property.exception.PropertyException;
import com.habiterra.property.repository.PropertyTypeRepository;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class PropertyTypeService {
    private final PropertyTypeRepository repository;
    private final PropertyAuthorizationService authorization;

    public PropertyTypeService(PropertyTypeRepository repository, PropertyAuthorizationService authorization) {
        this.repository = repository;
        this.authorization = authorization;
    }

    public List<PropertyTypeResponse> list() {
        return repository.findAll(Sort.by("libelle", "id")).stream().map(this::toResponse).toList();
    }

    public PropertyTypeResponse get(Long id) {
        return toResponse(repository.findById(id).orElseThrow(() ->
                new PropertyException(404, "PROPERTY_TYPE_NOT_FOUND", "Property type not found")));
    }

    @Transactional
    public PropertyTypeResponse create(CreatePropertyTypeRequest request, Authentication authentication) {
        authorization.requireManager(authorization.currentUser(authentication));
        TypeBien type = new TypeBien();
        type.setLibelle(request.label().strip());
        type.setDescription(request.description() == null ? null : request.description().strip());
        return toResponse(repository.save(type));
    }

    private PropertyTypeResponse toResponse(TypeBien type) {
        return new PropertyTypeResponse(type.getId(), type.getLibelle(), type.getDescription());
    }
}
