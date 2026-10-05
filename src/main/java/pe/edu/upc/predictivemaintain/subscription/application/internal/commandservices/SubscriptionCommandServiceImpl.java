package pe.edu.upc.predictivemaintain.subscription.application.internal.commandservices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.predictivemaintain.shared.application.errors.ApplicationException;
import pe.edu.upc.predictivemaintain.subscription.application.commandservices.SubscriptionCommandService;
import pe.edu.upc.predictivemaintain.subscription.application.errors.SubscriptionError;
import pe.edu.upc.predictivemaintain.subscription.domain.model.aggregates.Invoice;
import pe.edu.upc.predictivemaintain.subscription.domain.model.aggregates.Subscription;
import pe.edu.upc.predictivemaintain.subscription.domain.model.aggregates.SubscriptionPlan;
import pe.edu.upc.predictivemaintain.subscription.domain.model.commands.ChangeSubscriptionPlanCommand;
import pe.edu.upc.predictivemaintain.subscription.domain.model.commands.RenewSubscriptionCommand;
import pe.edu.upc.predictivemaintain.subscription.domain.model.valueobjects.SubscriptionStatus;
import pe.edu.upc.predictivemaintain.subscription.domain.repositories.CapacityReservationRepository;
import pe.edu.upc.predictivemaintain.subscription.domain.repositories.InvoiceRepository;
import pe.edu.upc.predictivemaintain.subscription.domain.repositories.SubscriptionPlanRepository;
import pe.edu.upc.predictivemaintain.subscription.domain.repositories.SubscriptionRepository;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
public class SubscriptionCommandServiceImpl implements SubscriptionCommandService {

    /** Length of one billing cycle (the same 30 days the initial subscription has). */
    private static final long BILLING_PERIOD_DAYS = 30;

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository planRepository;
    private final CapacityReservationRepository reservationRepository;
    private final InvoiceRepository invoiceRepository;
    private final Clock clock;

    public SubscriptionCommandServiceImpl(SubscriptionRepository subscriptionRepository,
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
    @Transactional
    public Subscription handle(ChangeSubscriptionPlanCommand command) {
        Instant now = clock.instant();
        // The same lock the asset reservations use: while the plan changes, nobody reserves a slot, so the
        // count of assets in use cannot change under our feet.
        Subscription subscription = lockSubscription(command.tenantId());
        SubscriptionPlan plan = planRepository.findById(command.planId())
                .orElseThrow(() -> new ApplicationException(SubscriptionError.PLAN_NOT_FOUND));

        if (subscription.getPlanId().equals(plan.getId())) {
            return subscription; // idempotent: asking again for the current plan changes nothing
        }
        if (subscription.getStatus() != SubscriptionStatus.ACTIVE) {
            throw new ApplicationException(SubscriptionError.SUBSCRIPTION_NOT_ACTIVE);
        }

        long occupied = reservationRepository.countOccupiedBySubscriptionId(subscription.getId());
        subscription.changePlan(plan, occupied);
        Subscription saved = subscriptionRepository.save(subscription);

        invoiceRepository.save(Invoice.issue(command.tenantId(), saved.getId(),
                "Plan change to " + plan.getName(), plan.getPrice(), now));
        return saved;
    }

    @Override
    @Transactional
    public Subscription handle(RenewSubscriptionCommand command) {
        Instant now = clock.instant();
        Subscription subscription = lockSubscription(command.tenantId());
        SubscriptionPlan plan = planRepository.findById(subscription.getPlanId())
                .orElseThrow(() -> new ApplicationException(SubscriptionError.PLAN_NOT_FOUND));

        subscription.renew(now, BILLING_PERIOD_DAYS);
        Subscription saved = subscriptionRepository.save(subscription);

        invoiceRepository.save(Invoice.issue(command.tenantId(), saved.getId(),
                "Renewal - " + plan.getName(), plan.getPrice(), now));
        return saved;
    }

    private Subscription lockSubscription(UUID tenantId) {
        return subscriptionRepository.findCurrentForUpdate(tenantId)
                .orElseThrow(() -> new ApplicationException(SubscriptionError.SUBSCRIPTION_NOT_FOUND));
    }
}