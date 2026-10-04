package pe.edu.upc.predictivemaintain.telemetry.domain.repositories;

import pe.edu.upc.predictivemaintain.shared.domain.model.valueobjects.PageQuery;
import pe.edu.upc.predictivemaintain.shared.domain.model.valueobjects.PagedResult;
import pe.edu.upc.predictivemaintain.telemetry.domain.model.aggregates.SensorReading;

import java.time.Instant;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface SensorReadingRepository {

    SensorReading save(SensorReading reading);

    Optional<SensorReading> findBySensorIdAndSourceKey(UUID tenantId, UUID sensorId, String sourceKey);

    /** The reading with the most recent measurement time. */
    Optional<SensorReading> findLatestBySensorId(UUID tenantId, UUID sensorId);

    /** Readings of several sensors between two instants (inclusive), newest first. */
    PagedResult<SensorReading> search(UUID tenantId, Collection<UUID> sensorIds, Instant from, Instant to,
                                      PageQuery page);
}