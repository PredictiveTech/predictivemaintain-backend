package pe.edu.upc.predictivemaintain.subscription.domain.model.aggregates;

import pe.edu.upc.predictivemaintain.shared.domain.exceptions.DomainConflictException;
import pe.edu.upc.predictivemaintain.shared.domain.exceptions.DomainValidationException;
import pe.edu.upc.predictivemaintain.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import pe.edu.upc.predictivemaintain.subscription.domain.model.valueobjects.SubscriptionStatus;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.UUID;

/**
 * Terms contracted by a company: plan, kept asset limit, state and validity period.
 */
public class Subscription extends AbstractDomainAggregateRoot {

    /** There is no real payment behind a renewal, so it cannot be bought far in advance. */
    private static final long MAX_AHEAD_DAYS = 366;

    private final UUID id;
    private final UUID tenantId;
    private UUID planId;
    private int assetLimit;
    private SubscriptionStatus status;
    private final Instant startsAt;
    private Instant endsAt;

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

    /** The validity period is over (independently of the status). */
    public boolean isExpired(Instant at) {
        return !at.isBefore(endsAt);
    }

    /**
     * Moves the company to another plan. The new limit cannot be smaller than the assets already in use:
     * the company would be left with more assets than it pays for (US-17, scenario 3).
     *
     * @param occupiedAssets reservations that currently hold a slot
     */
    public void changePlan(SubscriptionPlan newPlan, long occupiedAssets) {
        if (occupiedAssets > newPlan.getAssetLimit()) {
            throw new DomainConflictException("conflict.subscription.plan-below-usage",
                    String.valueOf(occupiedAssets), String.valueOf(newPlan.getAssetLimit()));
        }
        this.planId = newPlan.getId();
        this.assetLimit = newPlan.getAssetLimit();
    }

    /**
     * Extends the period. It starts from the current end if the subscription is still running, or from now if
     * it already expired, so the customer never pays for time that was already lost.
     */
    public void renew(Instant now, long periodDays) {
        if (status != SubscriptionStatus.ACTIVE) {
            throw new DomainConflictException("conflict.subscription.not-renewable");
        }
        Instant base = endsAt.isAfter(now) ? endsAt : now;
        Instant newEnd = base.plus(periodDays, ChronoUnit.DAYS);
        if (newEnd.isAfter(now.plus(MAX_AHEAD_DAYS, ChronoUnit.DAYS))) {
            throw new DomainConflictException("conflict.subscription.renewal-too-far");
        }
        this.endsAt = newEnd;
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