package pe.edu.upc.predictivemaintain.maintenance.domain.model.commands;

import java.util.UUID;

public record StartWorkOrderCommand(UUID tenantId, UUID actorId, UUID workOrderId, Long expectedVersion) {
}