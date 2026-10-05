package pe.edu.upc.predictivemaintain.telemetry.infrastructure.persistence.jpa.adapters;

import org.springframework.stereotype.Repository;
import pe.edu.upc.predictivemaintain.telemetry.domain.model.aggregates.AnomalyDetection;
import pe.edu.upc.predictivemaintain.telemetry.domain.repositories.AnomalyDetectionRepository;
import pe.edu.upc.predictivemaintain.telemetry.infrastructure.persistence.jpa.assemblers.AnomalyDetectionPersistenceAssembler;
import pe.edu.upc.predictivemaintain.telemetry.infrastructure.persistence.jpa.repositories.AnomalyDetectionPersistenceRepository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class AnomalyDetectionRepositoryImpl implements AnomalyDetectionRepository {

    private final AnomalyDetectionPersistenceRepository jpaRepository;

    public AnomalyDetectionRepositoryImpl(AnomalyDetectionPersistenceRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public AnomalyDetection save(AnomalyDetection detection) {
        return AnomalyDetectionPersistenceAssembler.toDomain(
                jpaRepository.save(AnomalyDetectionPersistenceAssembler.toEntity(detection)));
    }

    @Override
    public Optional<AnomalyDetection> findByReadingId(UUID tenantId, UUID readingId) {
        return jpaRepository.findByTenantIdAndReadingId(tenantId, readingId)
                .map(AnomalyDetectionPersistenceAssembler::toDomain);
    }
}