package pe.edu.upc.predictivemaintain.subscription.interfaces.rest.resources;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Public representation of a subscription plan.
 */
public record SubscriptionPlanResource(UUID id, String name, int assetLimit, BigDecimal amount, String currency) {
}