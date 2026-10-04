package pe.edu.upc.predictivemaintain.maintenance.domain.model.aggregates;

import pe.edu.upc.predictivemaintain.maintenance.domain.model.valueobjects.AlertDiagnostic;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.valueobjects.AlertSeverity;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.valueobjects.AlertStatus;
import pe.edu.upc.predictivemaintain.shared.domain.exceptions.DomainConflictException;
import pe.edu.upc.predictivemaintain.shared.domain.exceptions.DomainValidationException;
import pe.edu.upc.predictivemaintain.shared.domain.model.aggregates.AbstractVersionedAggregateRoot;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Operational warning about an asset. It is born automatically (IN_REVIEW) from an anomaly event;
 * the sourceEventId guarantees one event produces at most one alert.
 */
public class Alert extends AbstractVersionedAggregateRoot {

    private static final int MAX_REASON_LENGTH = 500;

    private final UUID id;
    private final UUID tenantId;
    private final UUID assetId;
    private final UUID sourceEventId;
    private final AlertSeverity severity;
    private AlertStatus status;
    private final Instant raisedAt;
    private String discardReason;
    private final AlertDiagnostic diagnostic;

    private Alert(UUID id, UUID tenantId, UUID assetId, UUID sourceEventId, AlertSeverity severity,
                  AlertStatus status, Instant raisedAt, String discardReason, AlertDiagnostic diagnostic,
                  long version) {
        super(version);
        this.id = Objects.requireNonNull(id);
        this.tenantId = Objects.requireNonNull(tenantId);
        this.assetId = Objects.requireNonNull(assetId);
        this.sourceEventId = Objects.requireNonNull(sourceEventId);
        this.severity = Objects.requireNonNull(severity);
        this.status = Objects.requireNonNull(status);
        this.raisedAt = Objects.requireNonNull(raisedAt);
        this.discardReason = discardReason;
        this.diagnostic = diagnostic;
    }

    /** The diagnostic may be null for alerts that do not come from a sensor reading. */
    public static Alert raise(UUID tenantId, UUID assetId, UUID sourceEventId, AlertSeverity severity,
                              AlertDiagnostic diagnostic, Instant now) {
        return new Alert(UUID.randomUUID(), tenantId, assetId, sourceEventId, severity,
                AlertStatus.IN_REVIEW, now, null, diagnostic, 0);
    }

    public static Alert restore(UUID id, UUID tenantId, UUID assetId, UUID sourceEventId, AlertSeverity severity,
                                AlertStatus status, Instant raisedAt, String discardReason,
                                AlertDiagnostic diagnostic, long version) {
        return new Alert(id, tenantId, assetId, sourceEventId, severity, status, raisedAt, discardReason,
                diagnostic, version);
    }

    /** The manager accepts the alert as real. It does not create the work order. */
    public void confirm() {
        transition(AlertStatus.IN_REVIEW, AlertStatus.CONFIRMED);
    }

    /** The manager rejects the alert. A reason is mandatory and kept. */
    public void discard(String reason) {
        String validReason = requireReason(reason, "validation.alert.discard-reason-required");
        transition(AlertStatus.IN_REVIEW, AlertStatus.DISCARDED);
        this.discardReason = validReason;
    }

    /** Used when the work order of a confirmed alert is cancelled: the incident is dismissed. */
    public void dismissByCancelledOrder(String reason) {
        String validReason = requireReason(reason, "validation.work-order.cancel-reason-required");
        transition(AlertStatus.CONFIRMED, AlertStatus.DISCARDED);
        this.discardReason = validReason;
    }

    /** Used when the work order of a confirmed alert is completed. */
    public void resolve() {
        transition(AlertStatus.CONFIRMED, AlertStatus.RESOLVED);
    }

    private void transition(AlertStatus expectedCurrent, AlertStatus target) {
        if (status != expectedCurrent) {
            throw new DomainConflictException("conflict.alert.invalid-transition", status.name(), target.name());
        }
        this.status = target;
    }

    private static String requireReason(String reason, String errorKey) {
        if (reason == null || reason.isBlank() || reason.trim().length() > MAX_REASON_LENGTH) {
            throw new DomainValidationException(errorKey);
        }
        return reason.trim();
    }

    public UUID getId() {
        return id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public UUID getAssetId() {
        return assetId;
    }

    public UUID getSourceEventId() {
        return sourceEventId;
    }

    public AlertSeverity getSeverity() {
        return severity;
    }

    public AlertStatus getStatus() {
        return status;
    }

    public Instant getRaisedAt() {
        return raisedAt;
    }

    public String getDiscardReason() {
        return discardReason;
    }

    public AlertDiagnostic getDiagnostic() {
        return diagnostic;
    }
}