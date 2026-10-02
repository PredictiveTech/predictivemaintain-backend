package pe.edu.upc.predictivemaintain.subscription.interfaces.rest.transform;

import pe.edu.upc.predictivemaintain.subscription.domain.model.aggregates.SubscriptionPlan;
import pe.edu.upc.predictivemaintain.subscription.interfaces.rest.resources.SubscriptionPlanResource;

public final class SubscriptionPlanResourceFromEntityAssembler {

    private SubscriptionPlanResourceFromEntityAssembler() {
    }

    public static SubscriptionPlanResource toResource(SubscriptionPlan plan) {
        return new SubscriptionPlanResource(
                plan.getId(),
                plan.getName(),
                plan.getAssetLimit(),
                plan.getPrice().amount(),
                plan.getPrice().currency());
    }
}