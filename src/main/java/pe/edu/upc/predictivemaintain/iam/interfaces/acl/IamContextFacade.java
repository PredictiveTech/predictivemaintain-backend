package pe.edu.upc.predictivemaintain.iam.interfaces.acl;

import org.springframework.stereotype.Service;
import pe.edu.upc.predictivemaintain.iam.domain.model.aggregates.UserAccount;
import pe.edu.upc.predictivemaintain.iam.domain.model.valueobjects.RoleName;
import pe.edu.upc.predictivemaintain.iam.domain.repositories.UserAccountRepository;

import java.util.UUID;

/**
 * Anti-Corruption Layer: what the IAM context exposes to other bounded contexts.
 */
@Service
public class IamContextFacade {

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
}