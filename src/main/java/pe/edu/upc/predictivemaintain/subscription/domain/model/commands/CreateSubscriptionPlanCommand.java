package pe.edu.upc.predictivemaintain.subscription.domain.model.commands;

import java.math.BigDecimal;

/**
 * Command to create a subscription plan.
 */
public record CreateSubscriptionPlanCommand(String name, int assetLimit, BigDecimal amount, String currency) {
}