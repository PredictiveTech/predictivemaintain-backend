package pe.edu.upc.predictivemaintain.maintenance.domain.model.queries;

import pe.edu.upc.predictivemaintain.shared.domain.model.valueobjects.PageQuery;

import java.util.UUID;

/**
 * Lists the assets of one company. Filters are optional; inactive assets are hidden unless requested.
 */
public record GetAllAssetsQuery(UUID tenantId, String productionLine, String assetType,
                                boolean includeInactive, PageQuery page) {
}