package pe.edu.upc.predictivemaintain.maintenance.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.valueobjects.AlertSeverity;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.valueobjects.AlertStatus;
import pe.edu.upc.predictivemaintain.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Database representation of Alert (table alerts). The unique pair (tenant_id, source_event_id)
 * is what guarantees one anomaly event creates at most one alert.
 */
@Entity
@Table(name = "alerts",
        uniqueConstraints = @UniqueConstraint(name = "uk_alerts_tenant_source_event",
                columnNames = {"tenant_id", "source_event_id"}),
        indexes = @Index(name = "idx_alerts_tenant_status_raised", columnList = "tenant_id, status, raised_at"))
public class AlertPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Id
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "asset_id", nullable = false)
    private UUID assetId;

    @Column(name = "source_event_id", nullable = false)
    private UUID sourceEventId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AlertSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AlertStatus status;

    @Column(name = "raised_at", nullable = false)
    private Instant raisedAt;

    @Column(name = "discard_reason", length = 500)
    private String discardReason;

    @Column(name = "diag_metric", length = 40)
    private String diagnosticMetric;

    @Column(name = "diag_unit", length = 20)
    private String diagnosticUnit;

    @Column(name = "diag_observed_value", precision = 18, scale = 6)
    private BigDecimal diagnosticObservedValue;

    @Column(name = "diag_lower_bound", precision = 18, scale = 6)
    private BigDecimal diagnosticLowerBound;

    @Column(name = "diag_upper_bound", precision = 18, scale = 6)
    private BigDecimal diagnosticUpperBound;

    @Column(name = "diag_measured_at")
    private Instant diagnosticMeasuredAt;

    /** Managed by JPA: it increases by one on every change and detects two people editing at once. */
    @Version
    private Long version;

    public AlertPersistenceEntity() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }

    public UUID getAssetId() {
        return assetId;
    }

    public void setAssetId(UUID assetId) {
        this.assetId = assetId;
    }

    public UUID getSourceEventId() {
        return sourceEventId;
    }

    public void setSourceEventId(UUID sourceEventId) {
        this.sourceEventId = sourceEventId;
    }

    public AlertSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(AlertSeverity severity) {
        this.severity = severity;
    }

    public AlertStatus getStatus() {
        return status;
    }

    public void setStatus(AlertStatus status) {
        this.status = status;
    }

    public Instant getRaisedAt() {
        return raisedAt;
    }

    public void setRaisedAt(Instant raisedAt) {
        this.raisedAt = raisedAt;
    }

    public String getDiscardReason() {
        return discardReason;
    }

    public void setDiscardReason(String discardReason) {
        this.discardReason = discardReason;
    }

    public String getDiagnosticMetric() {
        return diagnosticMetric;
    }

    public void setDiagnosticMetric(String diagnosticMetric) {
        this.diagnosticMetric = diagnosticMetric;
    }

    public String getDiagnosticUnit() {
        return diagnosticUnit;
    }

    public void setDiagnosticUnit(String diagnosticUnit) {
        this.diagnosticUnit = diagnosticUnit;
    }

    public BigDecimal getDiagnosticObservedValue() {
        return diagnosticObservedValue;
    }

    public void setDiagnosticObservedValue(BigDecimal diagnosticObservedValue) {
        this.diagnosticObservedValue = diagnosticObservedValue;
    }

    public BigDecimal getDiagnosticLowerBound() {
        return diagnosticLowerBound;
    }

    public void setDiagnosticLowerBound(BigDecimal diagnosticLowerBound) {
        this.diagnosticLowerBound = diagnosticLowerBound;
    }

    public BigDecimal getDiagnosticUpperBound() {
        return diagnosticUpperBound;
    }

    public void setDiagnosticUpperBound(BigDecimal diagnosticUpperBound) {
        this.diagnosticUpperBound = diagnosticUpperBound;
    }

    public Instant getDiagnosticMeasuredAt() {
        return diagnosticMeasuredAt;
    }

    public void setDiagnosticMeasuredAt(Instant diagnosticMeasuredAt) {
        this.diagnosticMeasuredAt = diagnosticMeasuredAt;
    }

    public Long getVersion() {
        return version;
    }
}