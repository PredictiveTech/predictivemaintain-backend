package pe.edu.upc.predictivemaintain.maintenance.domain.model.commands;

import java.util.UUID;

public record ConfirmAlertCommand(UUID tenantId, UUID alertId, Long expectedVersion) {
}