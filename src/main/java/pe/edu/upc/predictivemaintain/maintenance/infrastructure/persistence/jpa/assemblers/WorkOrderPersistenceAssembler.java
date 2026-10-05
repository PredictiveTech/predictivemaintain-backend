package pe.edu.upc.predictivemaintain.maintenance.infrastructure.persistence.jpa.assemblers;

import pe.edu.upc.predictivemaintain.maintenance.domain.model.aggregates.WorkOrder;
import pe.edu.upc.predictivemaintain.maintenance.infrastructure.persistence.jpa.entities.WorkOrderPersistenceEntity;

public final class WorkOrderPersistenceAssembler {

    private WorkOrderPersistenceAssembler() {
    }

    public static WorkOrder toDomain(WorkOrderPersistenceEntity entity) {
        return WorkOrder.restore(entity.getId(), entity.getTenantId(), entity.getAlertId(),
                entity.getAssignedUserId(), entity.getStatus(), entity.getSummary(), entity.getOpenedAt(),
                entity.getCompletedAt(), entity.getVersion());
    }

    /** The version is not copied: JPA owns it. */
    public static void copyToEntity(WorkOrder order, WorkOrderPersistenceEntity entity) {
        entity.setId(order.getId());
        entity.setTenantId(order.getTenantId());
        entity.setAlertId(order.getAlertId());
        entity.setAssignedUserId(order.getAssignedUserId());
        entity.setStatus(order.getStatus());
        entity.setSummary(order.getSummary());
        entity.setOpenedAt(order.getOpenedAt());
        entity.setCompletedAt(order.getCompletedAt());
    }
}