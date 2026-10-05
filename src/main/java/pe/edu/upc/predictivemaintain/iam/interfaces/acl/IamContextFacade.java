package pe.edu.upc.predictivemaintain.iam.interfaces.acl;

import org.springframework.stereotype.Service;
import pe.edu.upc.predictivemaintain.iam.domain.model.aggregates.UserAccount;
import pe.edu.upc.predictivemaintain.iam.domain.model.valueobjects.RoleName;
import pe.edu.upc.predictivemaintain.iam.domain.repositories.UserAccountRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Anti-Corruption Layer: what the IAM context exposes to other bounded contexts.
 */
@Service
public class IamContextFacade {

    /** The minimum another context needs to reach a person. */
    public record UserContact(UUID id, String email, String displayName) {
    }

    private final UserAccountRepository userAccountRepository;

    public IamContextFacade(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    /**
     * True when the user exists in that company, is active and has the TECHNICIAN role.
     * A user from another company is treated as if it did not exist.
     */
    public boolean isActiveTechnician(UUID tenantId, UUID userId) {
        return userAccountRepository.findByIdAndTenantId(userId, tenantId)
                .filter(UserAccount::isActive)
                .map(user -> user.hasRole(RoleName.TECHNICIAN))
                .orElse(false);
    }

    /** An active user of that company, or empty (also for users of another company). */
    public Optional<UserContact> findActiveUser(UUID tenantId, UUID userId) {
        return userAccountRepository.findByIdAndTenantId(userId, tenantId)
                .filter(UserAccount::isActive)
                .map(IamContextFacade::toContact);
    }

    /** The active maintenance managers of a company: the people to warn by default. */
    public List<UserContact> findActiveManagers(UUID tenantId) {
        return userAccountRepository.findActiveByTenantIdAndRole(tenantId, RoleName.MAINTENANCE_MANAGER).stream()
                .map(IamContextFacade::toContact)
                .toList();
    }

    private static UserContact toContact(UserAccount user) {
        return new UserContact(user.getId(), user.getEmail().value(), user.getDisplayName().value());
    }
}