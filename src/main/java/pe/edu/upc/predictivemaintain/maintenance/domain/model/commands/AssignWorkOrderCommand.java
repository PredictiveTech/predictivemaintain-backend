package pe.edu.upc.predictivemaintain.maintenance.domain.model.commands;

import java.util.UUID;

public record AssignWorkOrderCommand(UUID tenantId, UUID actorId, UUID workOrderId,
                                     UUID technicianId, Long expectedVersion) {
}