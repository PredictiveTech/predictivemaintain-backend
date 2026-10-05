package pe.edu.upc.predictivemaintain.maintenance.domain.model.commands;

import java.util.UUID;

public record CreateWorkOrderCommand(UUID tenantId, UUID actorId, UUID alertId) {
}