package pe.edu.upc.predictivemaintain.maintenance.domain.model.commands;

import pe.edu.upc.predictivemaintain.maintenance.domain.model.valueobjects.AlertSeverity;

import java.util.UUID;

/**
 * @param sourceEventId id of the anomaly event; repeating it returns the existing alert instead of creating another
 */
public record RegisterAlertCommand(UUID tenantId, UUID assetId, UUID sourceEventId, AlertSeverity severity) {
}