package pe.edu.upc.predictivemaintain.subscription.application.queryservices;

import pe.edu.upc.predictivemaintain.subscription.domain.model.aggregates.Subscription;
import pe.edu.upc.predictivemaintain.subscription.domain.model.aggregates.SubscriptionPlan;

import java.time.Duration;
import java.time.Instant;

/**
 * The subscription seen by its owner: plan, how much of the plan is used, and how close it is to expiring.
 * The warnings are computed at the moment of the query ({@code at}), so they are never out of date.
 */
public record SubscriptionOverview(Subscription subscription, SubscriptionPlan plan, long usedAssets, Instant at) {

    /** US-18, scenario 2: warn when 90% of the limit is reached. */
    private static final int NEAR_LIMIT_PERCENT = 90;

    /** US-19, scenario 1: warn when the subscription expires within 7 days. */
    private static final long EXPIRY_WARNING_DAYS = 7;

    public int availableAssets() {
        return (int) Math.max(0, subscription.getAssetLimit() - usedAssets);
    }

    public double usagePercent() {
        int limit = subscription.getAssetLimit();
        return limit == 0 ? 0 : Math.round(10000.0 * usedAssets / limit) / 100.0;
    }

    public boolean nearLimit() {
        return usedAssets * 100 >= (long) subscription.getAssetLimit() * NEAR_LIMIT_PERCENT;
    }

    public boolean expired() {
        return subscription.isExpired(at);
    }

    /** Whole days left, rounded up (23 hours left is "1 day"); 0 once it expired. */
    public long daysRemaining() {
        if (expired()) {
            return 0;
        }
        long seconds = Duration.between(at, subscription.getEndsAt()).getSeconds();
        return (seconds + 86_399) / 86_400;
    }

    public boolean expiresSoon() {
        return !expired() && daysRemaining() <= EXPIRY_WARNING_DAYS;
    }

    /** Whether the platform is currently accepting the company's monitoring data. */
    public boolean monitoringAllowed() {
        return subscription.isActive(at);
    }
}