package pe.edu.upc.predictivemaintain.maintenance.infrastructure.persistence.jpa.assemblers;

import pe.edu.upc.predictivemaintain.maintenance.domain.model.aggregates.Alert;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.valueobjects.AlertDiagnostic;
import pe.edu.upc.predictivemaintain.maintenance.infrastructure.persistence.jpa.entities.AlertPersistenceEntity;

public final class AlertPersistenceAssembler {

    private AlertPersistenceAssembler() {
    }

    public static Alert toDomain(AlertPersistenceEntity entity) {
        AlertDiagnostic diagnostic = entity.getDiagnosticMetric() == null
                ? null
                : new AlertDiagnostic(entity.getDiagnosticMetric(), entity.getDiagnosticUnit(),
                entity.getDiagnosticObservedValue(), entity.getDiagnosticLowerBound(),
                entity.getDiagnosticUpperBound(), entity.getDiagnosticMeasuredAt());
        return Alert.restore(entity.getId(), entity.getTenantId(), entity.getAssetId(), entity.getSourceEventId(),
                entity.getSeverity(), entity.getStatus(), entity.getRaisedAt(), entity.getDiscardReason(),
                diagnostic, entity.getVersion());
    }

    /** The version is not copied: JPA owns it. */
    public static void copyToEntity(Alert alert, AlertPersistenceEntity entity) {
        entity.setId(alert.getId());
        entity.setTenantId(alert.getTenantId());
        entity.setAssetId(alert.getAssetId());
        entity.setSourceEventId(alert.getSourceEventId());
        entity.setSeverity(alert.getSeverity());
        entity.setStatus(alert.getStatus());
        entity.setRaisedAt(alert.getRaisedAt());
        entity.setDiscardReason(alert.getDiscardReason());

        AlertDiagnostic diagnostic = alert.getDiagnostic();
        entity.setDiagnosticMetric(diagnostic == null ? null : diagnostic.metric());
        entity.setDiagnosticUnit(diagnostic == null ? null : diagnostic.unit());
        entity.setDiagnosticObservedValue(diagnostic == null ? null : diagnostic.observedValue());
        entity.setDiagnosticLowerBound(diagnostic == null ? null : diagnostic.lowerBound());
        entity.setDiagnosticUpperBound(diagnostic == null ? null : diagnostic.upperBound());
        entity.setDiagnosticMeasuredAt(diagnostic == null ? null : diagnostic.measuredAt());
    }
}