package pe.edu.upc.predictivemaintain.maintenance.interfaces.rest.resources;

import pe.edu.upc.predictivemaintain.maintenance.domain.model.valueobjects.AlertSeverity;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.valueobjects.AlertStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * @param assetCode  code of the asset the alert is about, ready to show in a list
 * @param assetName  name of that asset
 * @param version    send it back as expectedVersion to detect that someone else changed the alert meanwhile
 * @param diagnostic the data that originated the alert; null for alerts that did not come from a reading
 */
public record AlertResource(UUID id, UUID assetId, String assetCode, String assetName, AlertSeverity severity,
                            AlertStatus status, Instant raisedAt, String discardReason, long version,
                            AlertDiagnosticResource diagnostic) {
}