package pe.edu.upc.predictivemaintain.subscription.domain.model.aggregates;

import pe.edu.upc.predictivemaintain.shared.domain.exceptions.DomainValidationException;
import pe.edu.upc.predictivemaintain.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import pe.edu.upc.predictivemaintain.subscription.domain.model.valueobjects.SubscriptionStatus;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Terms contracted by a company: plan, kept asset limit, state and validity period.
 */
public class Subscription extends AbstractDomainAggregateRoot {

    private final UUID id;
    private final UUID tenantId;
    private final UUID planId;
    private final int assetLimit;
    private SubscriptionStatus status;
    private final Instant startsAt;
    private final Instant endsAt;

    private Subscription(UUID id, UUID tenantId, UUID planId, int assetLimit,
                         SubscriptionStatus status, Instant startsAt, Instant endsAt) {
        if (!Objects.requireNonNull(endsAt).isAfter(Objects.requireNonNull(startsAt))) {
            throw new DomainValidationException("validation.subscription.period-invalid");
        }
        this.id = Objects.requireNonNull(id);
        this.tenantId = Objects.requireNonNull(tenantId);
        this.planId = Objects.requireNonNull(planId);
        this.assetLimit = assetLimit;
        this.status = Objects.requireNonNull(status);
        this.startsAt = startsAt;
        this.endsAt = endsAt;
    }

    /** Starts an ACTIVE subscription keeping the asset limit the plan had at that moment. */
    public static Subscription start(UUID tenantId, SubscriptionPlan plan, Instant startsAt, Instant endsAt) {
        return new Subscription(UUID.randomUUID(), tenantId, plan.getId(), plan.getAssetLimit(),
                SubscriptionStatus.ACTIVE, startsAt, endsAt);
    }

    public static Subscription restore(UUID id, UUID tenantId, UUID planId, int assetLimit,
                                       SubscriptionStatus status, Instant startsAt, Instant endsAt) {
        return new Subscription(id, tenantId, planId, assetLimit, status, startsAt, endsAt);
    }

    /** Valid when ACTIVE and the instant is inside [startsAt, endsAt). */
    public boolean isActive(Instant at) {
        return status == SubscriptionStatus.ACTIVE && !at.isBefore(startsAt) && at.isBefore(endsAt);
    }

    public void suspend() {
        this.status = SubscriptionStatus.SUSPENDED;
    }

    public void cancel() {
        this.status = SubscriptionStatus.CANCELLED;
    }

    public UUID getId() {
        return id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public UUID getPlanId() {
        return planId;
    }

    public int getAssetLimit() {
        return assetLimit;
    }

    public SubscriptionStatus getStatus() {
        return status;
    }

    public Instant getStartsAt() {
        return startsAt;
    }

    public Instant getEndsAt() {
        return endsAt;
    }
}