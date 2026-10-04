package pe.edu.upc.predictivemaintain.telemetry.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.predictivemaintain.telemetry.infrastructure.persistence.jpa.entities.ThresholdRulePersistenceEntity;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ThresholdRulePersistenceRepository extends JpaRepository<ThresholdRulePersistenceEntity, UUID> {

    Optional<ThresholdRulePersistenceEntity> findByTenantIdAndSensorId(UUID tenantId, UUID sensorId);

    List<ThresholdRulePersistenceEntity> findByTenantIdAndSensorIdIn(UUID tenantId, Collection<UUID> sensorIds);
}