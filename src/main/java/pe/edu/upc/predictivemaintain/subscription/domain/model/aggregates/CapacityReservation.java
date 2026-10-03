package pe.edu.upc.predictivemaintain.subscription.domain.model.aggregates;

import pe.edu.upc.predictivemaintain.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import pe.edu.upc.predictivemaintain.subscription.domain.model.valueobjects.CapacityReservationStatus;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * One slot of the subscription's asset limit, held by one asset.
 */
public class CapacityReservation extends AbstractDomainAggregateRoot {

    private final UUID id;
    private final UUID tenantId;
    private final UUID subscriptionId;
    private final UUID operationId;
    private final UUID assetId;
    private CapacityReservationStatus status;
    private final Instant createdAt;

    private CapacityReservation(UUID id, UUID tenantId, UUID subscriptionId, UUID operationId,
                                UUID assetId, CapacityReservationStatus status, Instant createdAt) {
        this.id = Objects.requireNonNull(id);
        this.tenantId = Objects.requireNonNull(tenantId);
        this.subscriptionId = Objects.requireNonNull(subscriptionId);
        this.operationId = Objects.requireNonNull(operationId);
        this.assetId = Objects.requireNonNull(assetId);
        this.status = Objects.requireNonNull(status);
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    public static CapacityReservation confirmed(UUID tenantId, UUID subscriptionId, UUID operationId,
                                                UUID assetId, Instant createdAt) {
        return new CapacityReservation(UUID.randomUUID(), tenantId, subscriptionId, operationId, assetId,
                CapacityReservationStatus.CONFIRMED, createdAt);
    }

    public static CapacityReservation restore(UUID id, UUID tenantId, UUID subscriptionId, UUID operationId,
                                              UUID assetId, CapacityReservationStatus status, Instant createdAt) {
        return new CapacityReservation(id, tenantId, subscriptionId, operationId, assetId, status, createdAt);
    }

    /** Idempotent: releasing an already released reservation changes nothing. */
    public void release() {
        this.status = CapacityReservationStatus.RELEASED;
    }

    public UUID getId() {
        return id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public UUID getSubscriptionId() {
        return subscriptionId;
    }

    public UUID getOperationId() {
        return operationId;
    }

    public UUID getAssetId() {
        return assetId;
    }

    public CapacityReservationStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}