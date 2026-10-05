package pe.edu.upc.predictivemaintain.maintenance.domain.model.queries;

import pe.edu.upc.predictivemaintain.maintenance.domain.model.valueobjects.AssetStatus;
import pe.edu.upc.predictivemaintain.shared.domain.model.valueobjects.PageQuery;

import java.util.UUID;

/**
 * The assets of one company with their computed status. All filters are optional.
 *
 * @param sensorType name of a physical variable (for example TEMPERATURE); not case sensitive
 */
public record GetAssetOverviewsQuery(UUID tenantId, String productionLine, String assetType, AssetStatus status,
                                     String sensorType, boolean includeInactive, PageQuery page) {
}