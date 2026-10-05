package pe.edu.upc.predictivemaintain.maintenance.interfaces.rest.resources;

import pe.edu.upc.predictivemaintain.maintenance.domain.model.valueobjects.WorkOrderStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * @param summary corrective actions when COMPLETED, or the reason when CANCELLED
 * @param version send it back as expectedVersion to detect that someone else changed the order meanwhile
 */
public record WorkOrderResource(UUID id, UUID alertId, UUID assignedUserId, WorkOrderStatus status,
                                String summary, Instant openedAt, Instant completedAt, long version) {
}