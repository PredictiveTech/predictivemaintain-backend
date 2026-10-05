package pe.edu.upc.predictivemaintain.subscription.interfaces.rest.resources;

import pe.edu.upc.predictivemaintain.subscription.domain.model.valueobjects.SubscriptionStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * The subscription panel (US-18, US-19).
 *
 * @param monitoringAllowed false when the subscription expired or is not active: readings are being refused
 */
public record SubscriptionResource(UUID id, SubscriptionStatus status, Instant startsAt, Instant endsAt,
                                   boolean monitoringAllowed, PlanInfo plan, CapacityInfo capacity,
                                   ExpiryInfo expiry) {

    public record PlanInfo(UUID id, String name, int assetLimit, BigDecimal amount, String currency) {
    }

    /**
     * @param nearLimit true from 90% of the limit (US-18, scenario 2)
     */
    public record CapacityInfo(int limit, long used, int available, double usagePercent, boolean nearLimit) {
    }

    /**
     * @param expiresSoon true when 7 days or fewer remain (US-19, scenario 1)
     */
    public record ExpiryInfo(long daysRemaining, boolean expiresSoon, boolean expired) {
    }
}