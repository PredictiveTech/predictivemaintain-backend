package pe.edu.upc.predictivemaintain.maintenance.domain.model.aggregates;

import pe.edu.upc.predictivemaintain.maintenance.domain.model.valueobjects.WorkOrderStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Immutable record of one change in a work order: who did it, when, and what changed
 * (state and/or assigned technician). Changes are only added, never edited.
 */
public record WorkOrderChange(UUID id, UUID tenantId, UUID workOrderId, UUID actorId,
                              WorkOrderStatus previousStatus, WorkOrderStatus newStatus,
                              UUID previousAssigneeId, UUID newAssigneeId, Instant changedAt) {

    public static WorkOrderChange create(UUID tenantId, UUID workOrderId, UUID actorId,
                                         WorkOrderStatus previousStatus, WorkOrderStatus newStatus,
                                         UUID previousAssigneeId, UUID newAssigneeId, Instant changedAt) {
        return new WorkOrderChange(UUID.randomUUID(), tenantId, workOrderId, actorId, previousStatus,
                newStatus, previousAssigneeId, newAssigneeId, changedAt);
    }
}