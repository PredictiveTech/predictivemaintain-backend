package pe.edu.upc.predictivemaintain.telemetry.domain.model.queries;

import pe.edu.upc.predictivemaintain.shared.domain.model.valueobjects.PageQuery;

import java.time.Instant;
import java.util.UUID;

/**
 * Detections of an asset between two instants. Without from/to the last 7 days are used; the range
 * cannot exceed 31 days.
 */
public record GetAssetAnalyticsQuery(UUID tenantId, UUID assetId, Instant from, Instant to, PageQuery page) {
}