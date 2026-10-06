package pe.edu.upc.predictivemaintain.maintenance.domain.model.queries;

import java.util.Collection;
import java.util.UUID;

/**
 * The code and name of a set of assets, to put a readable label on lists of alerts.
 */
public record GetAssetLabelsQuery(UUID tenantId, Collection<UUID> assetIds) {
}