package pe.edu.upc.predictivemaintain.telemetry.infrastructure.persistence.jpa.assemblers;

import pe.edu.upc.predictivemaintain.telemetry.domain.model.aggregates.AnomalyDetection;
import pe.edu.upc.predictivemaintain.telemetry.infrastructure.persistence.jpa.entities.AnomalyDetectionPersistenceEntity;

public final class AnomalyDetectionPersistenceAssembler {

    private AnomalyDetectionPersistenceAssembler() {
    }

    public static AnomalyDetection toDomain(AnomalyDetectionPersistenceEntity entity) {
        return AnomalyDetection.restore(entity.getId(), entity.getTenantId(), entity.getReadingId(),
                entity.getRuleId(), entity.getRuleVersion(), entity.getLowerSnapshot(), entity.getUpperSnapshot(),
                entity.getSeverity(), entity.getDetectedAt(), entity.isAlertRaised());
    }

    public static AnomalyDetectionPersistenceEntity toEntity(AnomalyDetection detection) {
        AnomalyDetectionPersistenceEntity entity = new AnomalyDetectionPersistenceEntity();
        entity.setId(detection.getId());
        entity.setTenantId(detection.getTenantId());
        entity.setReadingId(detection.getReadingId());
        entity.setRuleId(detection.getRuleId());
        entity.setRuleVersion(detection.getRuleVersion());
        entity.setLowerSnapshot(detection.getLowerSnapshot());
        entity.setUpperSnapshot(detection.getUpperSnapshot());
        entity.setSeverity(detection.getSeverity());
        entity.setDetectedAt(detection.getDetectedAt());
        entity.setAlertRaised(detection.isAlertRaised());
        return entity;
    }
}