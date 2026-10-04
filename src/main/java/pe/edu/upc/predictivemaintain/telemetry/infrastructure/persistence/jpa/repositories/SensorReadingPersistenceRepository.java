package pe.edu.upc.predictivemaintain.telemetry.infrastructure.persistence.jpa.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.predictivemaintain.telemetry.infrastructure.persistence.jpa.entities.SensorReadingPersistenceEntity;

import java.time.Instant;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface SensorReadingPersistenceRepository extends JpaRepository<SensorReadingPersistenceEntity, UUID> {

    Optional<SensorReadingPersistenceEntity> findByTenantIdAndSensorIdAndSourceKey(
            UUID tenantId, UUID sensorId, String sourceKey);

    Optional<SensorReadingPersistenceEntity> findFirstByTenantIdAndSensorIdOrderByMeasuredAtDesc(
            UUID tenantId, UUID sensorId);

    Page<SensorReadingPersistenceEntity> findByTenantIdAndSensorIdInAndMeasuredAtBetween(
            UUID tenantId, Collection<UUID> sensorIds, Instant from, Instant to, Pageable pageable);
}