package pe.edu.upc.predictivemaintain.maintenance.infrastructure.persistence.jpa.assemblers;

import pe.edu.upc.predictivemaintain.maintenance.domain.model.aggregates.Alert;
import pe.edu.upc.predictivemaintain.maintenance.infrastructure.persistence.jpa.entities.AlertPersistenceEntity;

public final class AlertPersistenceAssembler {

    private AlertPersistenceAssembler() {
    }

    public static Alert toDomain(AlertPersistenceEntity entity) {
        return Alert.restore(entity.getId(), entity.getTenantId(), entity.getAssetId(), entity.getSourceEventId(),
                entity.getSeverity(), entity.getStatus(), entity.getRaisedAt(), entity.getDiscardReason(),
                entity.getVersion());
    }

    /** The version is not copied: JPA owns it. */
    public static void copyToEntity(Alert alert, AlertPersistenceEntity entity) {
        entity.setId(alert.getId());
        entity.setTenantId(alert.getTenantId());
        entity.setAssetId(alert.getAssetId());
        entity.setSourceEventId(alert.getSourceEventId());
        entity.setSeverity(alert.getSeverity());
        entity.setStatus(alert.getStatus());
        entity.setRaisedAt(alert.getRaisedAt());
        entity.setDiscardReason(alert.getDiscardReason());
    }
}