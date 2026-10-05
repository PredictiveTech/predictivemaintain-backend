package pe.edu.upc.predictivemaintain.telemetry.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import pe.edu.upc.predictivemaintain.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import pe.edu.upc.predictivemaintain.telemetry.domain.model.valueobjects.Metric;

import java.time.Instant;
import java.util.UUID;

/**
 * Database representation of Sensor (table sensors). asset_id points to Maintenance without a foreign key.
 */
@Entity
@Table(name = "sensors", indexes = @Index(name = "idx_sensors_tenant_asset", columnList = "tenant_id, asset_id"))
public class SensorPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Id
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "asset_id", nullable = false)
    private UUID assetId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private Metric metric;

    @Column(nullable = false, length = 20)
    private String unit;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "last_received_at")
    private Instant lastReceivedAt;

    @Column(name = "last_alert_at")
    private Instant lastAlertAt;

    @Column(name = "device_key_hash", nullable = false, length = 64)
    private String deviceKeyHash;

    public SensorPersistenceEntity() {
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

    public Metric getMetric() {
        return metric;
    }

    public void setMetric(Metric metric) {
        this.metric = metric;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Instant getLastReceivedAt() {
        return lastReceivedAt;
    }

    public void setLastReceivedAt(Instant lastReceivedAt) {
        this.lastReceivedAt = lastReceivedAt;
    }

    public Instant getLastAlertAt() {
        return lastAlertAt;
    }

    public void setLastAlertAt(Instant lastAlertAt) {
        this.lastAlertAt = lastAlertAt;
    }

    public String getDeviceKeyHash() {
        return deviceKeyHash;
    }

    public void setDeviceKeyHash(String deviceKeyHash) {
        this.deviceKeyHash = deviceKeyHash;
    }
}