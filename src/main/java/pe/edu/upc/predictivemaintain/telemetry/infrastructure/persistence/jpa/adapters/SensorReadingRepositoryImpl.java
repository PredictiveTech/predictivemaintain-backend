package pe.edu.upc.predictivemaintain.telemetry.infrastructure.persistence.jpa.adapters;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import pe.edu.upc.predictivemaintain.shared.domain.model.valueobjects.PageQuery;
import pe.edu.upc.predictivemaintain.shared.domain.model.valueobjects.PagedResult;
import pe.edu.upc.predictivemaintain.telemetry.domain.model.aggregates.SensorReading;
import pe.edu.upc.predictivemaintain.telemetry.domain.repositories.SensorReadingRepository;
import pe.edu.upc.predictivemaintain.telemetry.infrastructure.persistence.jpa.assemblers.SensorReadingPersistenceAssembler;
import pe.edu.upc.predictivemaintain.telemetry.infrastructure.persistence.jpa.entities.SensorReadingPersistenceEntity;
import pe.edu.upc.predictivemaintain.telemetry.infrastructure.persistence.jpa.repositories.SensorReadingPersistenceRepository;

import java.time.Instant;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

@Repository
public class SensorReadingRepositoryImpl implements SensorReadingRepository {

    private final SensorReadingPersistenceRepository jpaRepository;

    public SensorReadingRepositoryImpl(SensorReadingPersistenceRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public SensorReading save(SensorReading reading) {
        return SensorReadingPersistenceAssembler.toDomain(
                jpaRepository.save(SensorReadingPersistenceAssembler.toEntity(reading)));
    }

    @Override
    public Optional<SensorReading> findBySensorIdAndSourceKey(UUID tenantId, UUID sensorId, String sourceKey) {
        return jpaRepository.findByTenantIdAndSensorIdAndSourceKey(tenantId, sensorId, sourceKey.trim())
                .map(SensorReadingPersistenceAssembler::toDomain);
    }

    @Override
    public Optional<SensorReading> findLatestBySensorId(UUID tenantId, UUID sensorId) {
        return jpaRepository.findFirstByTenantIdAndSensorIdOrderByMeasuredAtDesc(tenantId, sensorId)
                .map(SensorReadingPersistenceAssembler::toDomain);
    }

    @Override
    public PagedResult<SensorReading> search(UUID tenantId, Collection<UUID> sensorIds, Instant from, Instant to,
                                             PageQuery pageQuery) {
        if (sensorIds.isEmpty()) {
            return new PagedResult<>(java.util.List.of(), 0, pageQuery.page(), pageQuery.size());
        }
        Page<SensorReadingPersistenceEntity> page = jpaRepository.findByTenantIdAndSensorIdInAndMeasuredAtBetween(
                tenantId, sensorIds, from, to,
                PageRequest.of(pageQuery.page(), pageQuery.size(), Sort.by(Sort.Direction.DESC, "measuredAt")));
        return new PagedResult<>(
                page.getContent().stream().map(SensorReadingPersistenceAssembler::toDomain).toList(),
                page.getTotalElements(),
                pageQuery.page(),
                pageQuery.size());
    }
}