package pe.edu.upc.predictivemaintain.telemetry.domain.model.aggregates;

import pe.edu.upc.predictivemaintain.shared.domain.exceptions.DomainValidationException;
import pe.edu.upc.predictivemaintain.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import pe.edu.upc.predictivemaintain.telemetry.domain.model.valueobjects.Measurement;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * One measurement sent by a sensor. It never changes after being created.
 */
public class SensorReading extends AbstractDomainAggregateRoot {

    /** Tolerance for devices whose clock is slightly ahead of the server's. */
    private static final Duration MAX_FUTURE_SKEW = Duration.ofMinutes(5);

    private final UUID id;
    private final UUID tenantId;
    private final UUID sensorId;
    private final String sourceKey;
    private final Measurement measurement;
    private final Instant measuredAt;
    private final Instant receivedAt;

    private SensorReading(UUID id, UUID tenantId, UUID sensorId, String sourceKey, Measurement measurement,
                          Instant measuredAt, Instant receivedAt) {
        this.id = Objects.requireNonNull(id);
        this.tenantId = Objects.requireNonNull(tenantId);
        this.sensorId = Objects.requireNonNull(sensorId);
        this.sourceKey = Objects.requireNonNull(sourceKey);
        this.measurement = Objects.requireNonNull(measurement);
        this.measuredAt = Objects.requireNonNull(measuredAt);
        this.receivedAt = Objects.requireNonNull(receivedAt);
    }

    /**
     * @param sourceKey idempotency key chosen by the device: sending the same key twice never stores two readings
     */
    public static SensorReading create(Sensor sensor, String sourceKey, Measurement measurement,
                                       Instant measuredAt, Instant receivedAt) {
        if (sourceKey == null || sourceKey.isBlank() || sourceKey.trim().length() > 100) {
            throw new DomainValidationException("validation.reading.source-key-invalid");
        }
        if (measuredAt == null || measuredAt.isAfter(receivedAt.plus(MAX_FUTURE_SKEW))) {
            throw new DomainValidationException("validation.reading.measured-at-invalid");
        }
        return new SensorReading(UUID.randomUUID(), sensor.getTenantId(), sensor.getId(), sourceKey.trim(),
                measurement, measuredAt, receivedAt);
    }

    public static SensorReading restore(UUID id, UUID tenantId, UUID sensorId, String sourceKey,
                                        Measurement measurement, Instant measuredAt, Instant receivedAt) {
        return new SensorReading(id, tenantId, sensorId, sourceKey, measurement, measuredAt, receivedAt);
    }

    public BigDecimal getValue() {
        return measurement.value();
    }

    public String getUnit() {
        return measurement.unit();
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

    public String getSourceKey() {
        return sourceKey;
    }

    public Measurement getMeasurement() {
        return measurement;
    }

    public Instant getMeasuredAt() {
        return measuredAt;
    }

    public Instant getReceivedAt() {
        return receivedAt;
    }
}