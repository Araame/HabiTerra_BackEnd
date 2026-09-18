package com.habiterra.property.service;

import com.habiterra.property.dto.*;
import com.habiterra.property.entity.*;
import org.springframework.stereotype.Component;

@Component
public class PropertyMapper {
    //Transform BienImmobilier into a Property before showing it to the client
    public PropertyResponse toResponse(BienImmobilier property) {
        var address = property.getAddress();
        var type = property.getType();
        var gallery = property.getGallery();
        var owner = property.getOwner();
        var agency = property.getAgency();
        return new PropertyResponse(property.getId(),
                property.getTitre(),
                property.getDescription(),
                property.getSuperficie(),
                property.getNombrePieces(),
                property.getNombreChambres(),
                property.getNombreSallesDeBain(),
                property.getMontantLoyer(),
                property.getMontantCaution(),
                property.getStatut(),
                property.getMeuble(),
                property.getColocationAutorisee(),
                property.getCapaciteColocation(),
                property.getDisponibleAPartirDu(),
                property.getDateCreation(),
                new PropertyTypeResponse(type.getId(), type.getLibelle(), type.getDescription()),
                new AddressResponse(address.getId(), address.getPays(), address.getVille(), address.getCommune(),
                        address.getQuartier(), address.getRue(), address.getLatitude(), address.getLongitude()),
                property.getRooms().stream().map(room -> new RoomResponse(room.getId(), room.getNom(),
                        room.getSuperficie(), room.getDescription())).toList(),
                new GalleryResponse(gallery.getId(), gallery.getTitre(),
                        gallery.getPhotos().stream().map(this::toPhotoResponse).toList()),
                new OwnerSummaryResponse(owner.getId(), owner.getPrenom() + " " + owner.getNom(), com.habiterra.identity.entity.Role.PROPRIETAIRE),
                agency == null ? null : new AgencySummaryResponse(agency.getId()));
    }

    //Return photo response
    public PhotoResponse toPhotoResponse(PhotoBien photo) {
        return new PhotoResponse(photo.getId(), photo.getUrl(), photo.getDescription());
    }
}
