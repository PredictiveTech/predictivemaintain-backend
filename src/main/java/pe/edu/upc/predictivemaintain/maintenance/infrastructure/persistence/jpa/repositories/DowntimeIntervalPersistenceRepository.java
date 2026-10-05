package pe.edu.upc.predictivemaintain.maintenance.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.predictivemaintain.maintenance.infrastructure.persistence.jpa.entities.DowntimeIntervalPersistenceEntity;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface DowntimeIntervalPersistenceRepository
        extends JpaRepository<DowntimeIntervalPersistenceEntity, UUID> {

    /** A stop overlaps [from, to] when it starts before the end and finishes after the beginning. */
    List<DowntimeIntervalPersistenceEntity> findByTenantIdAndAssetIdInAndStartedAtLessThanAndEndedAtGreaterThan(
            UUID tenantId, Collection<UUID> assetIds, Instant to, Instant from);
}