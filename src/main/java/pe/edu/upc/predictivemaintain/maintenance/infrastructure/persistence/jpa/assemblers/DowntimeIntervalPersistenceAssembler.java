package pe.edu.upc.predictivemaintain.maintenance.infrastructure.persistence.jpa.assemblers;

import pe.edu.upc.predictivemaintain.maintenance.domain.model.aggregates.DowntimeInterval;
import pe.edu.upc.predictivemaintain.maintenance.infrastructure.persistence.jpa.entities.DowntimeIntervalPersistenceEntity;

public final class DowntimeIntervalPersistenceAssembler {

    private DowntimeIntervalPersistenceAssembler() {
    }

    public static DowntimeInterval toDomain(DowntimeIntervalPersistenceEntity entity) {
        return DowntimeInterval.restore(entity.getId(), entity.getTenantId(), entity.getAssetId(),
                entity.getStartedAt(), entity.getEndedAt());
    }

    public static DowntimeIntervalPersistenceEntity toEntity(DowntimeInterval interval) {
        DowntimeIntervalPersistenceEntity entity = new DowntimeIntervalPersistenceEntity();
        entity.setId(interval.getId());
        entity.setTenantId(interval.getTenantId());
        entity.setAssetId(interval.getAssetId());
        entity.setStartedAt(interval.getStartedAt());
        entity.setEndedAt(interval.getEndedAt());
        return entity;
    }
}