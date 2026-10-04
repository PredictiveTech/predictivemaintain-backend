package pe.edu.upc.predictivemaintain.maintenance.domain.model.queries;

import pe.edu.upc.predictivemaintain.maintenance.domain.model.valueobjects.AlertSeverity;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.valueobjects.AlertStatus;
import pe.edu.upc.predictivemaintain.shared.domain.model.valueobjects.PageQuery;

import java.util.UUID;

/**
 * Lists the alerts of one company, newest first. All filters are optional.
 */
public record GetAllAlertsQuery(UUID tenantId, AlertSeverity severity, AlertStatus status,
                                UUID assetId, PageQuery page) {
}