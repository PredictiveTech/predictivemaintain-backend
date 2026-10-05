package pe.edu.upc.predictivemaintain.subscription.application.commandservices;

import pe.edu.upc.predictivemaintain.subscription.domain.model.aggregates.Subscription;
import pe.edu.upc.predictivemaintain.subscription.domain.model.commands.ChangeSubscriptionPlanCommand;
import pe.edu.upc.predictivemaintain.subscription.domain.model.commands.RenewSubscriptionCommand;

/**
 * Write use cases on the subscription of a company.
 */
public interface SubscriptionCommandService {

    /** Moves the company to another plan and issues the invoice. Asking for the current plan changes nothing. */
    Subscription handle(ChangeSubscriptionPlanCommand command);

    /** Extends the period by one billing cycle and issues the invoice. */
    Subscription handle(RenewSubscriptionCommand command);
}