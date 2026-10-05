package pe.edu.upc.predictivemaintain.telemetry.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.predictivemaintain.telemetry.infrastructure.persistence.jpa.entities.AnomalyDetectionPersistenceEntity;

import java.util.Optional;
import java.util.UUID;

public interface AnomalyDetectionPersistenceRepository
        extends JpaRepository<AnomalyDetectionPersistenceEntity, UUID> {

    Optional<AnomalyDetectionPersistenceEntity> findByTenantIdAndReadingId(UUID tenantId, UUID readingId);
}