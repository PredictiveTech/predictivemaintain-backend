package pe.edu.upc.predictivemaintain.maintenance.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import pe.edu.upc.predictivemaintain.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Database representation of ProductionWindow (table production_windows, as in the AV1).
 */
@Entity
@Table(name = "production_windows",
        indexes = @Index(name = "idx_production_tenant_asset_starts", columnList = "tenant_id, asset_id, starts_at"))
public class ProductionWindowPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Id
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "asset_id", nullable = false)
    private UUID assetId;

    @Column(name = "starts_at", nullable = false)
    private Instant startsAt;

    @Column(name = "ends_at", nullable = false)
    private Instant endsAt;

    @Column(name = "planned_seconds", nullable = false)
    private long plannedSeconds;

    @Column(name = "operating_seconds", nullable = false)
    private long operatingSeconds;

    @Column(name = "total_units", nullable = false)
    private long totalUnits;

    @Column(name = "good_units", nullable = false)
    private long goodUnits;

    @Column(name = "ideal_cycle_seconds", nullable = false, precision = 12, scale = 3)
    private BigDecimal idealCycleSeconds;

    public ProductionWindowPersistenceEntity() {
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

    public Instant getStartsAt() {
        return startsAt;
    }

    public void setStartsAt(Instant startsAt) {
        this.startsAt = startsAt;
    }

    public Instant getEndsAt() {
        return endsAt;
    }

    public void setEndsAt(Instant endsAt) {
        this.endsAt = endsAt;
    }

    public long getPlannedSeconds() {
        return plannedSeconds;
    }

    public void setPlannedSeconds(long plannedSeconds) {
        this.plannedSeconds = plannedSeconds;
    }

    public long getOperatingSeconds() {
        return operatingSeconds;
    }

    public void setOperatingSeconds(long operatingSeconds) {
        this.operatingSeconds = operatingSeconds;
    }

    public long getTotalUnits() {
        return totalUnits;
    }

    public void setTotalUnits(long totalUnits) {
        this.totalUnits = totalUnits;
    }

    public long getGoodUnits() {
        return goodUnits;
    }

    public void setGoodUnits(long goodUnits) {
        this.goodUnits = goodUnits;
    }

    public BigDecimal getIdealCycleSeconds() {
        return idealCycleSeconds;
    }

    public void setIdealCycleSeconds(BigDecimal idealCycleSeconds) {
        this.idealCycleSeconds = idealCycleSeconds;
    }
}