package pe.edu.upc.predictivemaintain.subscription.interfaces.rest.transform;

import pe.edu.upc.predictivemaintain.subscription.application.queryservices.SubscriptionOverview;
import pe.edu.upc.predictivemaintain.subscription.domain.model.aggregates.Invoice;
import pe.edu.upc.predictivemaintain.subscription.domain.model.aggregates.Subscription;
import pe.edu.upc.predictivemaintain.subscription.domain.model.aggregates.SubscriptionPlan;
import pe.edu.upc.predictivemaintain.subscription.interfaces.rest.resources.InvoiceResource;
import pe.edu.upc.predictivemaintain.subscription.interfaces.rest.resources.SubscriptionResource;

public final class SubscriptionResourceAssembler {

    private SubscriptionResourceAssembler() {
    }

    public static SubscriptionResource toResource(SubscriptionOverview overview) {
        Subscription subscription = overview.subscription();
        SubscriptionPlan plan = overview.plan();
        return new SubscriptionResource(
                subscription.getId(),
                subscription.getStatus(),
                subscription.getStartsAt(),
                subscription.getEndsAt(),
                overview.monitoringAllowed(),
                new SubscriptionResource.PlanInfo(plan.getId(), plan.getName(), plan.getAssetLimit(),
                        plan.getPrice().amount(), plan.getPrice().currency()),
                new SubscriptionResource.CapacityInfo(subscription.getAssetLimit(), overview.usedAssets(),
                        overview.availableAssets(), overview.usagePercent(), overview.nearLimit()),
                new SubscriptionResource.ExpiryInfo(overview.daysRemaining(), overview.expiresSoon(),
                        overview.expired()));
    }

    public static InvoiceResource toResource(Invoice invoice) {
        return new InvoiceResource(invoice.getId(), invoice.getNumber(), invoice.getConcept(),
                invoice.getTotal().amount(), invoice.getTotal().currency(), invoice.getStatus(),
                invoice.getIssuedAt(), invoice.getPaymentReference());
    }
}