package pe.edu.upc.predictivemaintain.maintenance.domain.model.commands;

import java.util.UUID;

public record CompleteWorkOrderCommand(UUID tenantId, UUID actorId, UUID workOrderId,
                                       String summary, Long expectedVersion) {
}