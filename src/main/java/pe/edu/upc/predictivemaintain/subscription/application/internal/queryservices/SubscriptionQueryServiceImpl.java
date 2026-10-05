package pe.edu.upc.predictivemaintain.subscription.application.internal.queryservices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.predictivemaintain.shared.application.errors.ApplicationException;
import pe.edu.upc.predictivemaintain.shared.domain.model.valueobjects.PagedResult;
import pe.edu.upc.predictivemaintain.subscription.application.errors.SubscriptionError;
import pe.edu.upc.predictivemaintain.subscription.application.queryservices.SubscriptionOverview;
import pe.edu.upc.predictivemaintain.subscription.application.queryservices.SubscriptionQueryService;
import pe.edu.upc.predictivemaintain.subscription.domain.model.aggregates.Invoice;
import pe.edu.upc.predictivemaintain.subscription.domain.model.aggregates.Subscription;
import pe.edu.upc.predictivemaintain.subscription.domain.model.aggregates.SubscriptionPlan;
import pe.edu.upc.predictivemaintain.subscription.domain.model.queries.GetInvoicesQuery;
import pe.edu.upc.predictivemaintain.subscription.domain.model.queries.GetSubscriptionQuery;
import pe.edu.upc.predictivemaintain.subscription.domain.repositories.CapacityReservationRepository;
import pe.edu.upc.predictivemaintain.subscription.domain.repositories.InvoiceRepository;
import pe.edu.upc.predictivemaintain.subscription.domain.repositories.SubscriptionPlanRepository;
import pe.edu.upc.predictivemaintain.subscription.domain.repositories.SubscriptionRepository;

import java.time.Clock;

@Service
public class SubscriptionQueryServiceImpl implements SubscriptionQueryService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository planRepository;
    private final CapacityReservationRepository reservationRepository;
    private final InvoiceRepository invoiceRepository;
    private final Clock clock;

    public SubscriptionQueryServiceImpl(SubscriptionRepository subscriptionRepository,
                                        SubscriptionPlanRepository planRepository,
                                        CapacityReservationRepository reservationRepository,
                                        InvoiceRepository invoiceRepository,
                                        Clock clock) {
        this.subscriptionRepository = subscriptionRepository;
        this.planRepository = planRepository;
        this.reservationRepository = reservationRepository;
        this.invoiceRepository = invoiceRepository;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public SubscriptionOverview handle(GetSubscriptionQuery query) {
        Subscription subscription = subscriptionRepository.findCurrentByTenantId(query.tenantId())
                .orElseThrow(() -> new ApplicationException(SubscriptionError.SUBSCRIPTION_NOT_FOUND));
        SubscriptionPlan plan = planRepository.findById(subscription.getPlanId())
                .orElseThrow(() -> new ApplicationException(SubscriptionError.PLAN_NOT_FOUND));
        long used = reservationRepository.countOccupiedBySubscriptionId(subscription.getId());
        return new SubscriptionOverview(subscription, plan, used, clock.instant());
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResult<Invoice> handle(GetInvoicesQuery query) {
        return invoiceRepository.findByTenantId(query.tenantId(), query.page());
    }
}