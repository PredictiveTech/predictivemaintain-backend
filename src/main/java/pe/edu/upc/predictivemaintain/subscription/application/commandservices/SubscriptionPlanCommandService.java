package pe.edu.upc.predictivemaintain.subscription.application.commandservices;

import pe.edu.upc.predictivemaintain.subscription.domain.model.commands.CreateSubscriptionPlanCommand;

import java.util.UUID;

/**
 * Write use cases for subscription plans.
 */
public interface SubscriptionPlanCommandService {

    UUID handle(CreateSubscriptionPlanCommand command);
}