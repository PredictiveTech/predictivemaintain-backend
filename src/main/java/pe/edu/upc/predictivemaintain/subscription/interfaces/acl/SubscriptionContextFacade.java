package pe.edu.upc.predictivemaintain.subscription.interfaces.acl;

import org.springframework.stereotype.Service;
import pe.edu.upc.predictivemaintain.subscription.application.commandservices.CapacityReservationCommandService;
import pe.edu.upc.predictivemaintain.subscription.application.commandservices.CompanyCommandService;
import pe.edu.upc.predictivemaintain.subscription.domain.model.aggregates.Company;
import pe.edu.upc.predictivemaintain.subscription.domain.model.commands.ProvisionCompanyCommand;
import pe.edu.upc.predictivemaintain.subscription.domain.model.commands.ReleaseCapacityCommand;
import pe.edu.upc.predictivemaintain.subscription.domain.model.commands.ReserveCapacityCommand;
import pe.edu.upc.predictivemaintain.subscription.domain.repositories.CompanyRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Anti-Corruption Layer: what the Subscription context exposes to other bounded contexts.
 * It only returns simple types, never internal domain objects.
 */
@Service
public class SubscriptionContextFacade {

    private final CompanyCommandService companyCommandService;
    private final CapacityReservationCommandService capacityCommandService;
    private final CompanyRepository companyRepository;

    public SubscriptionContextFacade(CompanyCommandService companyCommandService,
                                     CapacityReservationCommandService capacityCommandService,
                                     CompanyRepository companyRepository) {
        this.companyCommandService = companyCommandService;
        this.capacityCommandService = capacityCommandService;
        this.companyRepository = companyRepository;
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
}