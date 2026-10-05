package pe.edu.upc.predictivemaintain.telemetry.domain.model.aggregates;

import pe.edu.upc.predictivemaintain.shared.domain.exceptions.DomainValidationException;
import pe.edu.upc.predictivemaintain.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import pe.edu.upc.predictivemaintain.telemetry.domain.model.valueobjects.ThresholdSeverity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.UUID;

/**
 * Allowed interval for a sensor. A value equal to a limit is still inside; a value outside produces a detection.
 * The version grows every time the manager changes the rule, so each detection can say which version it used.
 */
public class ThresholdRule extends AbstractDomainAggregateRoot {

    private static final BigDecimal MAX_ABSOLUTE_VALUE = new BigDecimal("1000000000000");

    private final UUID id;
    private final UUID tenantId;
    private final UUID sensorId;
    private BigDecimal lowerBound;
    private BigDecimal upperBound;
    private ThresholdSeverity severity;
    private int version;

    private ThresholdRule(UUID id, UUID tenantId, UUID sensorId, BigDecimal lowerBound, BigDecimal upperBound,
                          ThresholdSeverity severity, int version) {
        this.id = Objects.requireNonNull(id);
        this.tenantId = Objects.requireNonNull(tenantId);
        this.sensorId = Objects.requireNonNull(sensorId);
        this.version = version;
        apply(lowerBound, upperBound, severity);
    }

    public static ThresholdRule create(UUID tenantId, UUID sensorId, BigDecimal lowerBound,
                                       BigDecimal upperBound, ThresholdSeverity severity) {
        return new ThresholdRule(UUID.randomUUID(), tenantId, sensorId, lowerBound, upperBound, severity, 1);
    }

    public static ThresholdRule restore(UUID id, UUID tenantId, UUID sensorId, BigDecimal lowerBound,
                                        BigDecimal upperBound, ThresholdSeverity severity, int version) {
        return new ThresholdRule(id, tenantId, sensorId, lowerBound, upperBound, severity, version);
    }

    /** Replaces the interval and increases the version. */
    public void change(BigDecimal newLowerBound, BigDecimal newUpperBound, ThresholdSeverity newSeverity) {
        apply(newLowerBound, newUpperBound, newSeverity);
        this.version++;
    }

    public boolean isOutOfRange(BigDecimal value) {
        return value.compareTo(lowerBound) < 0 || value.compareTo(upperBound) > 0;
    }

    private void apply(BigDecimal newLowerBound, BigDecimal newUpperBound, ThresholdSeverity newSeverity) {
        // Everything is validated first, so a failure leaves the rule untouched.
        if (newSeverity == null) {
            throw new DomainValidationException("validation.threshold.severity-required");
        }
        if (newLowerBound == null || newUpperBound == null
                || newLowerBound.abs().compareTo(MAX_ABSOLUTE_VALUE) >= 0
                || newUpperBound.abs().compareTo(MAX_ABSOLUTE_VALUE) >= 0) {
            throw new DomainValidationException("validation.threshold.value-invalid");
        }
        BigDecimal lower = newLowerBound.setScale(6, RoundingMode.HALF_UP);
        BigDecimal upper = newUpperBound.setScale(6, RoundingMode.HALF_UP);
        if (lower.compareTo(upper) >= 0) {
            throw new DomainValidationException("validation.threshold.range-invalid");
        }
        this.lowerBound = lower;
        this.upperBound = upper;
        this.severity = newSeverity;
    }

    public UUID getId() {
        return id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public UUID getSensorId() {
        return sensorId;
    }

    public BigDecimal getLowerBound() {
        return lowerBound;
    }

    public BigDecimal getUpperBound() {
        return upperBound;
    }

    public ThresholdSeverity getSeverity() {
        return severity;
    }

    public int getVersion() {
        return version;
    }
}