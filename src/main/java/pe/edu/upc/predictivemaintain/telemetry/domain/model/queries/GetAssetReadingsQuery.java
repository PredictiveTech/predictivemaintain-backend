package pe.edu.upc.predictivemaintain.telemetry.domain.model.queries;

import pe.edu.upc.predictivemaintain.shared.domain.model.valueobjects.PageQuery;

import java.time.Instant;
import java.util.UUID;

/**
 * Readings of the sensors of one asset. When from/to are missing the last 24 hours are returned;
 * the range cannot exceed 31 days. sensorId optionally restricts the answer to one sensor.
 */
public record GetAssetReadingsQuery(UUID tenantId, UUID assetId, UUID sensorId, Instant from, Instant to,
                                    PageQuery page) {
}