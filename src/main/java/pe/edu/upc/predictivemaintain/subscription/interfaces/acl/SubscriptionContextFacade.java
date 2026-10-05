package pe.edu.upc.predictivemaintain.subscription.interfaces.acl;

import org.springframework.stereotype.Service;
import pe.edu.upc.predictivemaintain.shared.application.errors.ApplicationException;
import pe.edu.upc.predictivemaintain.subscription.application.commandservices.CapacityReservationCommandService;
import pe.edu.upc.predictivemaintain.subscription.application.commandservices.CompanyCommandService;
import pe.edu.upc.predictivemaintain.subscription.application.errors.SubscriptionError;
import pe.edu.upc.predictivemaintain.subscription.domain.model.aggregates.Company;
import pe.edu.upc.predictivemaintain.subscription.domain.model.commands.ProvisionCompanyCommand;
import pe.edu.upc.predictivemaintain.subscription.domain.model.commands.ReleaseCapacityCommand;
import pe.edu.upc.predictivemaintain.subscription.domain.model.commands.ReserveCapacityCommand;
import pe.edu.upc.predictivemaintain.subscription.domain.repositories.CompanyRepository;
import pe.edu.upc.predictivemaintain.subscription.domain.repositories.SubscriptionRepository;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

/**
 * Anti-Corruption Layer: what the Subscription context exposes to other bounded contexts.
 * It only returns simple types, never internal domain objects.
 */
@Service
public class SubscriptionContextFacade {

    private final CompanyCommandService companyCommandService;
    private final CapacityReservationCommandService capacityCommandService;
    private final CompanyRepository companyRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final Clock clock;

    public SubscriptionContextFacade(CompanyCommandService companyCommandService,
                                     CapacityReservationCommandService capacityCommandService,
                                     CompanyRepository companyRepository,
                                     SubscriptionRepository subscriptionRepository,
                                     Clock clock) {
        this.companyCommandService = companyCommandService;
        this.capacityCommandService = capacityCommandService;
        this.companyRepository = companyRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.clock = clock;
    }

    /**
     * Creates a company with its initial subscription and returns its tenantId.
     * Idempotent by registrationId.
     */
    public UUID provisionCompany(String companyName, UUID registrationId) {
        return companyCommandService.handle(new ProvisionCompanyCommand(companyName, registrationId)).getId();
    }

    /**
     * Finds the tenantId of a company created with the given registrationId, if any.
     */
    public Optional<UUID> findTenantIdByRegistrationId(UUID registrationId) {
        return companyRepository.findByRegistrationId(registrationId).map(Company::getId);
    }

    /**
     * Reserves one asset slot of the company's plan and returns the reservation id.
     * The assetId doubles as the idempotency key. Fails with CAPACITY_EXCEEDED when the plan is full.
     */
    public UUID reserveAssetCapacity(UUID tenantId, UUID assetId) {
        return capacityCommandService.handle(new ReserveCapacityCommand(tenantId, assetId, assetId));
    }

    /**
     * Releases a slot. Idempotent.
     */
    public void releaseAssetCapacity(UUID tenantId, UUID reservationId) {
        capacityCommandService.handle(new ReleaseCapacityCommand(tenantId, reservationId));
    }

    /**
     * True when the company has a subscription that is ACTIVE and inside its validity period (US-19).
     */
    public boolean isMonitoringAllowed(UUID tenantId) {
        Instant now = clock.instant();
        return subscriptionRepository.findCurrentByTenantId(tenantId)
                .map(subscription -> subscription.isActive(now))
                .orElse(false);
    }

    /**
     * Same question, but failing with SUBSCRIPTION_NOT_ACTIVE (403) when the answer is no. The rule belongs to
     * this context, so the error does too: callers do not need to know how it is decided.
     */
    public void requireMonitoringAllowed(UUID tenantId) {
        if (!isMonitoringAllowed(tenantId)) {
            throw new ApplicationException(SubscriptionError.SUBSCRIPTION_NOT_ACTIVE);
        }
    }

    /** A subscription that is about to end, used to warn its company (US-19). */
    public record ExpiringSubscription(UUID tenantId, UUID subscriptionId, Instant endsAt) {
    }

    /** ACTIVE subscriptions that end after "from" and not after "to". */
    public List<ExpiringSubscription> findExpiringBetween(Instant from, Instant to) {
        return subscriptionRepository.findActiveEndingBetween(from, to).stream()
                .map(subscription -> new ExpiringSubscription(
                        subscription.getTenantId(), subscription.getId(), subscription.getEndsAt()))
                .toList();
    }
}