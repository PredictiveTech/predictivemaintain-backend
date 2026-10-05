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
import java.util.UUID;

/**
 * Database representation of ThresholdRule (table threshold_rules). One rule per sensor.
 */
@Entity
@Table(name = "threshold_rules",
        uniqueConstraints = @UniqueConstraint(name = "uk_threshold_tenant_sensor",
                columnNames = {"tenant_id", "sensor_id"}))
public class ThresholdRulePersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Id
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "sensor_id", nullable = false)
    private UUID sensorId;

    @Column(name = "lower_bound", nullable = false, precision = 18, scale = 6)
    private BigDecimal lowerBound;

    @Column(name = "upper_bound", nullable = false, precision = 18, scale = 6)
    private BigDecimal upperBound;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ThresholdSeverity severity;

    @Column(name = "rule_version", nullable = false)
    private int ruleVersion;

    public ThresholdRulePersistenceEntity() {
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

    public BigDecimal getLowerBound() {
        return lowerBound;
    }

    public void setLowerBound(BigDecimal lowerBound) {
        this.lowerBound = lowerBound;
    }

    public BigDecimal getUpperBound() {
        return upperBound;
    }

    public void setUpperBound(BigDecimal upperBound) {
        this.upperBound = upperBound;
    }

    public ThresholdSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(ThresholdSeverity severity) {
        this.severity = severity;
    }

    public int getRuleVersion() {
        return ruleVersion;
    }

    public void setRuleVersion(int ruleVersion) {
        this.ruleVersion = ruleVersion;
    }
}