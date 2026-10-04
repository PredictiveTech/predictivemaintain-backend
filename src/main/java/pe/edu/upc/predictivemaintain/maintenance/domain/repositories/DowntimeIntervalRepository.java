package pe.edu.upc.predictivemaintain.maintenance.domain.repositories;

import pe.edu.upc.predictivemaintain.maintenance.domain.model.aggregates.DowntimeInterval;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface DowntimeIntervalRepository {

    DowntimeInterval save(DowntimeInterval interval);

    /** Stops that overlap the range [from, to], even partially. */
    List<DowntimeInterval> findOverlapping(UUID tenantId, Collection<UUID> assetIds, Instant from, Instant to);
}