package pe.edu.upc.predictivemaintain.maintenance.domain.repositories;

import pe.edu.upc.predictivemaintain.maintenance.domain.model.aggregates.Alert;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Read-only queries that cross alerts and assets, used by the reports and the asset list.
 */
public interface AssetActivityRepository {

    /** Alerts of the given assets raised between two instants, oldest first. */
    List<Alert> findAlertsRaisedBetween(UUID tenantId, Collection<UUID> assetIds, Instant from, Instant to);

    /** Ids (among the given ones) of the assets that have an alert IN_REVIEW or CONFIRMED. */
    Set<UUID> findAssetIdsWithOpenAlerts(UUID tenantId, Collection<UUID> assetIds);
}