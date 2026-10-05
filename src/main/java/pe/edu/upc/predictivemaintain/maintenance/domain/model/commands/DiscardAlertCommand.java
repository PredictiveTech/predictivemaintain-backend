package pe.edu.upc.predictivemaintain.maintenance.domain.model.commands;

import java.util.UUID;

public record DiscardAlertCommand(UUID tenantId, UUID alertId, String reason, Long expectedVersion) {
}