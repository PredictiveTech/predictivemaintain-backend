package pe.edu.upc.predictivemaintain.telemetry.infrastructure.persistence.jpa.adapters;

import org.springframework.stereotype.Repository;
import pe.edu.upc.predictivemaintain.shared.domain.services.DomainEventPublisher;
import pe.edu.upc.predictivemaintain.telemetry.domain.model.aggregates.Sensor;
import pe.edu.upc.predictivemaintain.telemetry.domain.repositories.SensorRepository;
import pe.edu.upc.predictivemaintain.telemetry.infrastructure.persistence.jpa.assemblers.SensorPersistenceAssembler;
import pe.edu.upc.predictivemaintain.telemetry.infrastructure.persistence.jpa.entities.SensorPersistenceEntity;
import pe.edu.upc.predictivemaintain.telemetry.infrastructure.persistence.jpa.repositories.SensorPersistenceRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class SensorRepositoryImpl implements SensorRepository {

    private final SensorPersistenceRepository jpaRepository;
    private final DomainEventPublisher eventPublisher;

    public SensorRepositoryImpl(SensorPersistenceRepository jpaRepository, DomainEventPublisher eventPublisher) {
        this.jpaRepository = jpaRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Sensor save(Sensor sensor) {
        SensorPersistenceEntity entity = jpaRepository.findById(sensor.getId())
                .orElseGet(SensorPersistenceEntity::new);
        SensorPersistenceAssembler.copyToEntity(sensor, entity);
        SensorPersistenceEntity saved = jpaRepository.save(entity);
        eventPublisher.publishAll(sensor);
        return SensorPersistenceAssembler.toDomain(saved);
    }

    @Override
    public Optional<Sensor> findById(UUID id) {
        return jpaRepository.findById(id).map(SensorPersistenceAssembler::toDomain);
    }

    @Override
    public Optional<Sensor> findByIdAndTenantId(UUID id, UUID tenantId) {
        return jpaRepository.findByIdAndTenantId(id, tenantId).map(SensorPersistenceAssembler::toDomain);
    }

    @Override
    public List<Sensor> findByAssetId(UUID tenantId, UUID assetId) {
        return jpaRepository.findByTenantIdAndAssetIdOrderByMetricAscIdAsc(tenantId, assetId).stream()
                .map(SensorPersistenceAssembler::toDomain)
                .toList();
    }
}