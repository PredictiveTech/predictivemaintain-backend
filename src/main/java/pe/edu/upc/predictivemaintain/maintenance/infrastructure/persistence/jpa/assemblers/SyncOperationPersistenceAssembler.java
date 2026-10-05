package pe.edu.upc.predictivemaintain.maintenance.infrastructure.persistence.jpa.assemblers;

import pe.edu.upc.predictivemaintain.maintenance.domain.model.aggregates.SyncOperationRecord;
import pe.edu.upc.predictivemaintain.maintenance.infrastructure.persistence.jpa.entities.SyncOperationPersistenceEntity;

public final class SyncOperationPersistenceAssembler {

    private SyncOperationPersistenceAssembler() {
    }

    public static SyncOperationRecord toDomain(SyncOperationPersistenceEntity entity) {
        return new SyncOperationRecord(entity.getId(), entity.getTenantId(), entity.getOperationId(),
                entity.getWorkOrderId(), entity.getType(), entity.getOutcome(), entity.getActionTime(),
                entity.getProcessedAt());
    }

    public static SyncOperationPersistenceEntity toEntity(SyncOperationRecord record) {
        SyncOperationPersistenceEntity entity = new SyncOperationPersistenceEntity();
        entity.setId(record.id());
        entity.setTenantId(record.tenantId());
        entity.setOperationId(record.operationId());
        entity.setWorkOrderId(record.workOrderId());
        entity.setType(record.type());
        entity.setOutcome(record.outcome());
        entity.setActionTime(record.actionTime());
        entity.setProcessedAt(record.processedAt());
        return entity;
    }
}