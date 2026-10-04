package pe.edu.upc.predictivemaintain.maintenance.infrastructure.persistence.jpa.adapters;

import org.springframework.stereotype.Repository;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.aggregates.DowntimeInterval;
import pe.edu.upc.predictivemaintain.maintenance.domain.repositories.DowntimeIntervalRepository;
import pe.edu.upc.predictivemaintain.maintenance.infrastructure.persistence.jpa.assemblers.DowntimeIntervalPersistenceAssembler;
import pe.edu.upc.predictivemaintain.maintenance.infrastructure.persistence.jpa.repositories.DowntimeIntervalPersistenceRepository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public class DowntimeIntervalRepositoryImpl implements DowntimeIntervalRepository {

    private final DowntimeIntervalPersistenceRepository jpaRepository;

    public DowntimeIntervalRepositoryImpl(DowntimeIntervalPersistenceRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public DowntimeInterval save(DowntimeInterval interval) {
        return DowntimeIntervalPersistenceAssembler.toDomain(
                jpaRepository.save(DowntimeIntervalPersistenceAssembler.toEntity(interval)));
    }

    @Override
    public List<DowntimeInterval> findOverlapping(UUID tenantId, Collection<UUID> assetIds, Instant from, Instant to) {
        if (assetIds.isEmpty()) {
            return List.of();
        }
        return jpaRepository
                .findByTenantIdAndAssetIdInAndStartedAtLessThanAndEndedAtGreaterThan(tenantId, assetIds, to, from)
                .stream()
                .map(DowntimeIntervalPersistenceAssembler::toDomain)
                .toList();
    }
}