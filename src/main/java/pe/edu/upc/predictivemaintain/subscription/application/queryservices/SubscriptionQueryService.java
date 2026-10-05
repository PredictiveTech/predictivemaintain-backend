package pe.edu.upc.predictivemaintain.subscription.application.queryservices;

import pe.edu.upc.predictivemaintain.shared.domain.model.valueobjects.PagedResult;
import pe.edu.upc.predictivemaintain.subscription.domain.model.aggregates.Invoice;
import pe.edu.upc.predictivemaintain.subscription.domain.model.queries.GetInvoicesQuery;
import pe.edu.upc.predictivemaintain.subscription.domain.model.queries.GetSubscriptionQuery;

public interface SubscriptionQueryService {

    SubscriptionOverview handle(GetSubscriptionQuery query);

    PagedResult<Invoice> handle(GetInvoicesQuery query);
}