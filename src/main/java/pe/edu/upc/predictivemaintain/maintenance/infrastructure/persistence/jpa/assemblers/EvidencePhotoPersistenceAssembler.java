package pe.edu.upc.predictivemaintain.maintenance.infrastructure.persistence.jpa.assemblers;

import pe.edu.upc.predictivemaintain.maintenance.domain.model.aggregates.EvidencePhoto;
import pe.edu.upc.predictivemaintain.maintenance.infrastructure.persistence.jpa.entities.EvidencePhotoPersistenceEntity;

public final class EvidencePhotoPersistenceAssembler {

    private EvidencePhotoPersistenceAssembler() {
    }

    public static EvidencePhoto toDomain(EvidencePhotoPersistenceEntity entity) {
        return new EvidencePhoto(entity.getId(), entity.getTenantId(), entity.getWorkOrderId(),
                entity.getStorageKey(), entity.getMimeType(), entity.getUploadedAt());
    }

    public static EvidencePhotoPersistenceEntity toEntity(EvidencePhoto photo) {
        EvidencePhotoPersistenceEntity entity = new EvidencePhotoPersistenceEntity();
        entity.setId(photo.id());
        entity.setTenantId(photo.tenantId());
        entity.setWorkOrderId(photo.workOrderId());
        entity.setStorageKey(photo.storageKey());
        entity.setMimeType(photo.mimeType());
        entity.setUploadedAt(photo.uploadedAt());
        return entity;
    }
}