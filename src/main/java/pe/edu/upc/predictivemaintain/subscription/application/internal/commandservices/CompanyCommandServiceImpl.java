package pe.edu.upc.predictivemaintain.subscription.application.internal.commandservices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.predictivemaintain.shared.application.errors.ApplicationException;
import pe.edu.upc.predictivemaintain.subscription.application.commandservices.CompanyCommandService;
import pe.edu.upc.predictivemaintain.subscription.application.errors.SubscriptionError;
import pe.edu.upc.predictivemaintain.subscription.domain.model.aggregates.Company;
import pe.edu.upc.predictivemaintain.subscription.domain.model.aggregates.Invoice;
import pe.edu.upc.predictivemaintain.subscription.domain.model.aggregates.Subscription;
import pe.edu.upc.predictivemaintain.subscription.domain.model.aggregates.SubscriptionPlan;
import pe.edu.upc.predictivemaintain.subscription.domain.model.commands.ProvisionCompanyCommand;
import pe.edu.upc.predictivemaintain.subscription.domain.repositories.CompanyRepository;
import pe.edu.upc.predictivemaintain.subscription.domain.repositories.InvoiceRepository;
import pe.edu.upc.predictivemaintain.subscription.domain.repositories.SubscriptionPlanRepository;
import pe.edu.upc.predictivemaintain.subscription.domain.repositories.SubscriptionRepository;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
public class CompanyCommandServiceImpl implements CompanyCommandService {

    /** Plan every new company starts with. */
    private static final String INITIAL_PLAN_NAME = "Basic";

    /** Length of the initial subscription period (decision of this guide, adjust if the business says otherwise). */
    private static final long INITIAL_PERIOD_DAYS = 30;

    private final CompanyRepository companyRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository planRepository;
    private final InvoiceRepository invoiceRepository;
    private final Clock clock;

    public CompanyCommandServiceImpl(CompanyRepository companyRepository,
                                     SubscriptionRepository subscriptionRepository,
                                     SubscriptionPlanRepository planRepository,
                                     InvoiceRepository invoiceRepository,
                                     Clock clock) {
        this.companyRepository = companyRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.planRepository = planRepository;
        this.invoiceRepository = invoiceRepository;
        this.clock = clock;
    }

    @Override
    @Transactional
    public Company handle(ProvisionCompanyCommand command) {
        Optional<Company> existing = companyRepository.findByRegistrationId(command.registrationId());
        if (existing.isPresent()) {
            return existing.get();
        }
        SubscriptionPlan plan = planRepository.findByName(INITIAL_PLAN_NAME)
                .orElseThrow(() -> new ApplicationException(SubscriptionError.PLAN_NOT_FOUND));

        Company company = companyRepository.save(Company.create(command.companyName(), command.registrationId()));

        Instant now = clock.instant();
        Subscription subscription = subscriptionRepository.save(
                Subscription.start(company.getId(), plan, now, now.plus(INITIAL_PERIOD_DAYS, ChronoUnit.DAYS)));
        invoiceRepository.save(Invoice.issue(company.getId(), subscription.getId(),
                "Initial subscription - " + plan.getName(), plan.getPrice(), now));
        return company;
    }
}