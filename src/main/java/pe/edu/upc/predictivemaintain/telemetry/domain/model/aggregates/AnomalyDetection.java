package pe.edu.upc.predictivemaintain.telemetry.domain.model.aggregates;

import pe.edu.upc.predictivemaintain.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import pe.edu.upc.predictivemaintain.telemetry.domain.model.valueobjects.ThresholdSeverity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Result of a reading that fell outside the allowed interval. It keeps the version and the limits that were
 * applied, so the record stays true even if the manager changes the rule later.
 */
public class AnomalyDetection extends AbstractDomainAggregateRoot {

    private final UUID id;
    private final UUID tenantId;
    private final UUID readingId;
    private final UUID ruleId;
    private final int ruleVersion;
    private final BigDecimal lowerSnapshot;
    private final BigDecimal upperSnapshot;
    private final ThresholdSeverity severity;
    private final Instant detectedAt;
    private boolean alertRaised;

    private AnomalyDetection(UUID id, UUID tenantId, UUID readingId, UUID ruleId, int ruleVersion,
                             BigDecimal lowerSnapshot, BigDecimal upperSnapshot, ThresholdSeverity severity,
                             Instant detectedAt, boolean alertRaised) {
        this.id = Objects.requireNonNull(id);
        this.tenantId = Objects.requireNonNull(tenantId);
        this.readingId = Objects.requireNonNull(readingId);
        this.ruleId = Objects.requireNonNull(ruleId);
        this.ruleVersion = ruleVersion;
        this.lowerSnapshot = Objects.requireNonNull(lowerSnapshot);
        this.upperSnapshot = Objects.requireNonNull(upperSnapshot);
        this.severity = Objects.requireNonNull(severity);
        this.detectedAt = Objects.requireNonNull(detectedAt);
        this.alertRaised = alertRaised;
    }

    public static AnomalyDetection fromThreshold(SensorReading reading, ThresholdRule rule, Instant detectedAt) {
        if (!reading.getTenantId().equals(rule.getTenantId()) || !reading.getSensorId().equals(rule.getSensorId())) {
            throw new IllegalArgumentException("The reading and the rule belong to different sensors");
        }
        return new AnomalyDetection(UUID.randomUUID(), reading.getTenantId(), reading.getId(), rule.getId(),
                rule.getVersion(), rule.getLowerBound(), rule.getUpperBound(), rule.getSeverity(),
                detectedAt, false);
    }

    public static AnomalyDetection restore(UUID id, UUID tenantId, UUID readingId, UUID ruleId, int ruleVersion,
                                           BigDecimal lowerSnapshot, BigDecimal upperSnapshot,
                                           ThresholdSeverity severity, Instant detectedAt, boolean alertRaised) {
        return new AnomalyDetection(id, tenantId, readingId, ruleId, ruleVersion, lowerSnapshot, upperSnapshot,
                severity, detectedAt, alertRaised);
    }

    /** The detection did produce an alert (it does not when the sensor is still in its waiting period). */
    public void markAlertRaised() {
        this.alertRaised = true;
    }

    public UUID getId() {
        return id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public UUID getReadingId() {
        return readingId;
    }

    public UUID getRuleId() {
        return ruleId;
    }

    public int getRuleVersion() {
        return ruleVersion;
    }

    public BigDecimal getLowerSnapshot() {
        return lowerSnapshot;
    }

    public BigDecimal getUpperSnapshot() {
        return upperSnapshot;
    }

    public ThresholdSeverity getSeverity() {
        return severity;
    }

    public Instant getDetectedAt() {
        return detectedAt;
    }

    public boolean isAlertRaised() {
        return alertRaised;
    }
}