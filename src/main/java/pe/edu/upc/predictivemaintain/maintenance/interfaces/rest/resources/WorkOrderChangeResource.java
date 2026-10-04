package pe.edu.upc.predictivemaintain.maintenance.interfaces.rest.resources;

import pe.edu.upc.predictivemaintain.maintenance.domain.model.valueobjects.WorkOrderStatus;

import java.time.Instant;
import java.util.UUID;

public record WorkOrderChangeResource(UUID id, UUID actorId, WorkOrderStatus previousStatus,
                                      WorkOrderStatus newStatus, UUID previousAssigneeId,
                                      UUID newAssigneeId, Instant changedAt) {
}