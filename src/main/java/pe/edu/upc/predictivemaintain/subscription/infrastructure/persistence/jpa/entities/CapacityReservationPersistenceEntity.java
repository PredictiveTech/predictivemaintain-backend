package pe.edu.upc.predictivemaintain.subscription.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import pe.edu.upc.predictivemaintain.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import pe.edu.upc.predictivemaintain.subscription.domain.model.valueobjects.CapacityReservationStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Database representation of CapacityReservation (table capacity_reservations).
 * The unique pair (tenant_id, operation_id) is what makes the reservation idempotent.
 */
@Entity
@Table(name = "capacity_reservations",
        uniqueConstraints = @UniqueConstraint(name = "uk_capacity_tenant_operation",
                columnNames = {"tenant_id", "operation_id"}))
public class CapacityReservationPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Id
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "subscription_id", nullable = false)
    private UUID subscriptionId;

    @Column(name = "operation_id", nullable = false)
    private UUID operationId;

    @Column(name = "asset_id", nullable = false)
    private UUID assetId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CapacityReservationStatus status;

    @Column(name = "reserved_at", nullable = false)
    private Instant reservedAt;

    public CapacityReservationPersistenceEntity() {
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

    public UUID getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(UUID subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public UUID getOperationId() {
        return operationId;
    }

    public void setOperationId(UUID operationId) {
        this.operationId = operationId;
    }

    public UUID getAssetId() {
        return assetId;
    }

    public void setAssetId(UUID assetId) {
        this.assetId = assetId;
    }

    public CapacityReservationStatus getStatus() {
        return status;
    }

    public void setStatus(CapacityReservationStatus status) {
        this.status = status;
    }

    public Instant getReservedAt() {
        return reservedAt;
    }

    public void setReservedAt(Instant reservedAt) {
        this.reservedAt = reservedAt;
    }
}