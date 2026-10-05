package pe.edu.upc.predictivemaintain.telemetry.domain.repositories;

import pe.edu.upc.predictivemaintain.shared.domain.model.valueobjects.PageQuery;
import pe.edu.upc.predictivemaintain.shared.domain.model.valueobjects.PagedResult;
import pe.edu.upc.predictivemaintain.telemetry.domain.model.valueobjects.DetectionView;

import java.time.Instant;
import java.util.Collection;
import java.util.UUID;

/**
 * Read-only queries that cross detections and readings.
 */
public interface TelemetryAnalyticsRepository {

    /** Detections of the given sensors between two instants, newest first. */
    PagedResult<DetectionView> findDetections(UUID tenantId, Collection<UUID> sensorIds, Instant from, Instant to,
                                              PageQuery page);
}