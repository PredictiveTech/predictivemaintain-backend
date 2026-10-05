package pe.edu.upc.predictivemaintain.telemetry.domain.model.aggregates;

import pe.edu.upc.predictivemaintain.shared.domain.exceptions.DomainValidationException;
import pe.edu.upc.predictivemaintain.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import pe.edu.upc.predictivemaintain.telemetry.domain.model.valueobjects.Metric;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Source of one series of measurements: one physical variable of one asset. It authenticates itself
 * with a device key; only the hash of that key is stored.
 */
public class Sensor extends AbstractDomainAggregateRoot {

    private final UUID id;
    private final UUID tenantId;
    private final UUID assetId;
    private final Metric metric;
    private final String unit;
    private boolean active;
    private Instant lastReceivedAt;
    private Instant lastAlertAt;
    private final String deviceKeyHash;

    private Sensor(UUID id, UUID tenantId, UUID assetId, Metric metric, String unit, boolean active,
                   Instant lastReceivedAt, Instant lastAlertAt, String deviceKeyHash) {
        if (metric == null) {
            throw new DomainValidationException("validation.sensor.metric-required");
        }
        if (unit == null || unit.isBlank() || unit.trim().length() > 20) {
            throw new DomainValidationException("validation.sensor.unit-invalid");
        }
        this.id = Objects.requireNonNull(id);
        this.tenantId = Objects.requireNonNull(tenantId);
        this.assetId = Objects.requireNonNull(assetId);
        this.metric = metric;
        this.unit = unit.trim();
        this.active = active;
        this.lastReceivedAt = lastReceivedAt;
        this.lastAlertAt = lastAlertAt;
        this.deviceKeyHash = Objects.requireNonNull(deviceKeyHash);
    }

    public static Sensor register(UUID tenantId, UUID assetId, Metric metric, String unit, String deviceKeyHash) {
        return new Sensor(UUID.randomUUID(), tenantId, assetId, metric, unit, true, null, null, deviceKeyHash);
    }

    public static Sensor restore(UUID id, UUID tenantId, UUID assetId, Metric metric, String unit, boolean active,
                                 Instant lastReceivedAt, Instant lastAlertAt, String deviceKeyHash) {
        return new Sensor(id, tenantId, assetId, metric, unit, active, lastReceivedAt, lastAlertAt, deviceKeyHash);
    }

    public void deactivate() {
        this.active = false;
    }

    /** Readings must use exactly the unit configured for the sensor: values of different units are never mixed. */
    public boolean acceptsUnit(String candidateUnit) {
        return candidateUnit != null && unit.equals(candidateUnit.trim());
    }

    /** Compares hashes in constant time, so the comparison time does not reveal how many characters matched. */
    public boolean matchesDeviceKeyHash(String candidateHash) {
        return candidateHash != null && MessageDigest.isEqual(
                deviceKeyHash.getBytes(StandardCharsets.UTF_8), candidateHash.getBytes(StandardCharsets.UTF_8));
    }

    public void recordReception(Instant at) {
        this.lastReceivedAt = Objects.requireNonNull(at);
    }

    /** A sensor that never sent anything is not communicating (US-01). */
    public boolean isCommunicating(Instant now, Duration timeout) {
        return lastReceivedAt != null && !lastReceivedAt.plus(timeout).isBefore(now);
    }

    /** Limits how many alerts one sensor can raise: after one, the next must wait for the cooldown. */
    public boolean canRaiseAlert(Instant now, Duration cooldown) {
        return lastAlertAt == null || !lastAlertAt.plus(cooldown).isAfter(now);
    }

    public void recordAlert(Instant at) {
        this.lastAlertAt = Objects.requireNonNull(at);
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

    public Metric getMetric() {
        return metric;
    }

    public String getUnit() {
        return unit;
    }

    public boolean isActive() {
        return active;
    }

    public Instant getLastReceivedAt() {
        return lastReceivedAt;
    }

    public Instant getLastAlertAt() {
        return lastAlertAt;
    }

    public String getDeviceKeyHash() {
        return deviceKeyHash;
    }
}