package pe.edu.upc.predictivemaintain.telemetry.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import pe.edu.upc.predictivemaintain.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import pe.edu.upc.predictivemaintain.telemetry.domain.model.valueobjects.ThresholdSeverity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Database representation of AnomalyDetection (table anomaly_detections). One detection per reading.
 */
@Entity
@Table(name = "anomaly_detections",
        uniqueConstraints = @UniqueConstraint(name = "uk_detections_tenant_reading",
                columnNames = {"tenant_id", "reading_id"}))
public class AnomalyDetectionPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Id
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "reading_id", nullable = false)
    private UUID readingId;

    @Column(name = "rule_id", nullable = false)
    private UUID ruleId;

    @Column(name = "rule_version", nullable = false)
    private int ruleVersion;

    @Column(name = "lower_snapshot", nullable = false, precision = 18, scale = 6)
    private BigDecimal lowerSnapshot;

    @Column(name = "upper_snapshot", nullable = false, precision = 18, scale = 6)
    private BigDecimal upperSnapshot;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ThresholdSeverity severity;

    @Column(name = "detected_at", nullable = false)
    private Instant detectedAt;

    @Column(name = "alert_raised", nullable = false)
    private boolean alertRaised;

    public AnomalyDetectionPersistenceEntity() {
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

    public UUID getReadingId() {
        return readingId;
    }

    public void setReadingId(UUID readingId) {
        this.readingId = readingId;
    }

    public UUID getRuleId() {
        return ruleId;
    }

    public void setRuleId(UUID ruleId) {
        this.ruleId = ruleId;
    }

    public int getRuleVersion() {
        return ruleVersion;
    }

    public void setRuleVersion(int ruleVersion) {
        this.ruleVersion = ruleVersion;
    }

    public BigDecimal getLowerSnapshot() {
        return lowerSnapshot;
    }

    public void setLowerSnapshot(BigDecimal lowerSnapshot) {
        this.lowerSnapshot = lowerSnapshot;
    }

    public BigDecimal getUpperSnapshot() {
        return upperSnapshot;
    }

    public void setUpperSnapshot(BigDecimal upperSnapshot) {
        this.upperSnapshot = upperSnapshot;
    }

    public ThresholdSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(ThresholdSeverity severity) {
        this.severity = severity;
    }

    public Instant getDetectedAt() {
        return detectedAt;
    }

    public void setDetectedAt(Instant detectedAt) {
        this.detectedAt = detectedAt;
    }

    public boolean isAlertRaised() {
        return alertRaised;
    }

    public void setAlertRaised(boolean alertRaised) {
        this.alertRaised = alertRaised;
    }
}