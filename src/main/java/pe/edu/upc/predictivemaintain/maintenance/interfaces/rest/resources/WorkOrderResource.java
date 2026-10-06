package pe.edu.upc.predictivemaintain.maintenance.interfaces.rest.resources;

import pe.edu.upc.predictivemaintain.maintenance.domain.model.valueobjects.AlertSeverity;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.valueobjects.WorkOrderStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * @param assetId   the asset the order is about (the one of its alert)
 * @param assetCode code of that asset, ready to show in a list
 * @param assetName name of that asset
 * @param severity  severity of the alert that originated the order
 * @param summary   corrective actions when COMPLETED, or the reason when CANCELLED
 * @param version   send it back as expectedVersion to detect that someone else changed the order meanwhile
 */
public record WorkOrderResource(UUID id, UUID alertId, UUID assetId, String assetCode, String assetName,
                                AlertSeverity severity, UUID assignedUserId, WorkOrderStatus status,
                                String summary, Instant openedAt, Instant completedAt, long version) {
}