package pe.edu.upc.predictivemaintain.telemetry.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import pe.edu.upc.predictivemaintain.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Database representation of SensorReading (table sensor_readings). The unique triple
 * (tenant_id, sensor_id, source_key) is what makes the ingestion idempotent.
 */
@Entity
@Table(name = "sensor_readings",
        uniqueConstraints = @UniqueConstraint(name = "uk_readings_tenant_sensor_source",
                columnNames = {"tenant_id", "sensor_id", "source_key"}),
        indexes = @Index(name = "idx_readings_tenant_sensor_measured",
                columnList = "tenant_id, sensor_id, measured_at"))
public class SensorReadingPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Id
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "sensor_id", nullable = false)
    private UUID sensorId;

    @Column(name = "source_key", nullable = false, length = 100)
    private String sourceKey;

    @Column(name = "reading_value", nullable = false, precision = 18, scale = 6)
    private BigDecimal value;

    @Column(nullable = false, length = 20)
    private String unit;

    @Column(name = "measured_at", nullable = false)
    private Instant measuredAt;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    public SensorReadingPersistenceEntity() {
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

    public UUID getSensorId() {
        return sensorId;
    }

    public void setSensorId(UUID sensorId) {
        this.sensorId = sensorId;
    }

    public String getSourceKey() {
        return sourceKey;
    }

    public void setSourceKey(String sourceKey) {
        this.sourceKey = sourceKey;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Instant getMeasuredAt() {
        return measuredAt;
    }

    public void setMeasuredAt(Instant measuredAt) {
        this.measuredAt = measuredAt;
    }

    public Instant getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(Instant receivedAt) {
        this.receivedAt = receivedAt;
    }
}