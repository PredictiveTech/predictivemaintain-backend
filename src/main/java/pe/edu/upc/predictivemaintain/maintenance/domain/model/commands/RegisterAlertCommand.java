package pe.edu.upc.predictivemaintain.maintenance.domain.model.commands;

import pe.edu.upc.predictivemaintain.maintenance.domain.model.valueobjects.AlertDiagnostic;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.valueobjects.AlertSeverity;

import java.util.UUID;

/**
 * @param sourceEventId id of the anomaly event; repeating it returns the existing alert instead of creating another
 * @param diagnostic    data that originated the alert (may be null)
 */
public record RegisterAlertCommand(UUID tenantId, UUID assetId, UUID sourceEventId, AlertSeverity severity,
                                   AlertDiagnostic diagnostic) {
}