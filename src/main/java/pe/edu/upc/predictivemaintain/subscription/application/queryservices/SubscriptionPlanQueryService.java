package pe.edu.upc.predictivemaintain.subscription.application.queryservices;

import pe.edu.upc.predictivemaintain.subscription.domain.model.aggregates.SubscriptionPlan;
import pe.edu.upc.predictivemaintain.subscription.domain.model.queries.GetAllSubscriptionPlansQuery;

import java.util.List;

/**
 * Read use cases for subscription plans.
 */
public interface SubscriptionPlanQueryService {

    List<SubscriptionPlan> handle(GetAllSubscriptionPlansQuery query);
}