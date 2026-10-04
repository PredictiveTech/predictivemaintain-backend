package pe.edu.upc.predictivemaintain.telemetry.domain.repositories;

import pe.edu.upc.predictivemaintain.telemetry.domain.model.aggregates.AnomalyDetection;

import java.util.Optional;
import java.util.UUID;

public interface AnomalyDetectionRepository {

    AnomalyDetection save(AnomalyDetection detection);

    Optional<AnomalyDetection> findByReadingId(UUID tenantId, UUID readingId);
}