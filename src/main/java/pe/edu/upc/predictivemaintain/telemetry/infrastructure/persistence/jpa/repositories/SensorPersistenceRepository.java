package pe.edu.upc.predictivemaintain.telemetry.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.predictivemaintain.telemetry.infrastructure.persistence.jpa.entities.SensorPersistenceEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SensorPersistenceRepository extends JpaRepository<SensorPersistenceEntity, UUID> {

    Optional<SensorPersistenceEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    List<SensorPersistenceEntity> findByTenantIdAndAssetIdOrderByMetricAscIdAsc(UUID tenantId, UUID assetId);
}