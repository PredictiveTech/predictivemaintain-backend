package pe.edu.upc.predictivemaintain.maintenance.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.valueobjects.Criticality;
import pe.edu.upc.predictivemaintain.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;

import java.util.UUID;

/**
 * Database representation of Asset (table assets).
 */
@Entity
@Table(name = "assets",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_assets_tenant_code", columnNames = {"tenant_id", "code"}),
                @UniqueConstraint(name = "uk_assets_reservation", columnNames = {"capacity_reservation_id"})},
        indexes = @Index(name = "idx_assets_tenant", columnList = "tenant_id"))
public class AssetPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Id
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 120)
    private String location;

    @Column(name = "plant_id")
    private UUID plantId;

    @Column(name = "production_line", length = 100)
    private String productionLine;

    @Column(name = "asset_type", nullable = false, length = 80)
    private String assetType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Criticality criticality;

    private Double latitude;

    private Double longitude;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "capacity_reservation_id", nullable = false)
    private UUID capacityReservationId;

    public AssetPersistenceEntity() {
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public UUID getPlantId() {
        return plantId;
    }

    public void setPlantId(UUID plantId) {
        this.plantId = plantId;
    }

    public String getProductionLine() {
        return productionLine;
    }

    public void setProductionLine(String productionLine) {
        this.productionLine = productionLine;
    }

    public String getAssetType() {
        return assetType;
    }

    public void setAssetType(String assetType) {
        this.assetType = assetType;
    }

    public Criticality getCriticality() {
        return criticality;
    }

    public void setCriticality(Criticality criticality) {
        this.criticality = criticality;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public UUID getCapacityReservationId() {
        return capacityReservationId;
    }

    public void setCapacityReservationId(UUID capacityReservationId) {
        this.capacityReservationId = capacityReservationId;
    }
}