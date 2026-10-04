package pe.edu.upc.predictivemaintain.maintenance.domain.model.commands;

import java.util.UUID;

public record CancelWorkOrderCommand(UUID tenantId, UUID actorId, UUID workOrderId,
                                     String reason, Long expectedVersion) {
}