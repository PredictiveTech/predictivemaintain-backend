package pe.edu.upc.predictivemaintain.maintenance.infrastructure.persistence.jpa.assemblers;

import pe.edu.upc.predictivemaintain.maintenance.domain.model.aggregates.WorkOrderChange;
import pe.edu.upc.predictivemaintain.maintenance.infrastructure.persistence.jpa.entities.WorkOrderChangePersistenceEntity;

public final class WorkOrderChangePersistenceAssembler {

    private WorkOrderChangePersistenceAssembler() {
    }

    public static WorkOrderChange toDomain(WorkOrderChangePersistenceEntity entity) {
        return new WorkOrderChange(entity.getId(), entity.getTenantId(), entity.getWorkOrderId(),
                entity.getActorId(), entity.getPreviousStatus(), entity.getNewStatus(),
                entity.getPreviousAssigneeId(), entity.getNewAssigneeId(), entity.getChangedAt());
    }

    public static WorkOrderChangePersistenceEntity toEntity(WorkOrderChange change) {
        WorkOrderChangePersistenceEntity entity = new WorkOrderChangePersistenceEntity();
        entity.setId(change.id());
        entity.setTenantId(change.tenantId());
        entity.setWorkOrderId(change.workOrderId());
        entity.setActorId(change.actorId());
        entity.setPreviousStatus(change.previousStatus());
        entity.setNewStatus(change.newStatus());
        entity.setPreviousAssigneeId(change.previousAssigneeId());
        entity.setNewAssigneeId(change.newAssigneeId());
        entity.setChangedAt(change.changedAt());
        return entity;
    }
}