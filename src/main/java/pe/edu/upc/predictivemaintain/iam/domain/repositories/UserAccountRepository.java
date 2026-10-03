package pe.edu.upc.predictivemaintain.iam.domain.repositories;

import pe.edu.upc.predictivemaintain.iam.domain.model.aggregates.UserAccount;
import pe.edu.upc.predictivemaintain.iam.domain.model.valueobjects.EmailAddress;
import pe.edu.upc.predictivemaintain.iam.domain.model.valueobjects.RoleName;

import java.util.Optional;
import java.util.UUID;

public interface UserAccountRepository {

    UserAccount save(UserAccount user);

    Optional<UserAccount> findById(UUID id);

    /** Lookup limited to one company: use it for any operation on behalf of a manager. */
    Optional<UserAccount> findByIdAndTenantId(UUID id, UUID tenantId);

    Optional<UserAccount> findByEmail(EmailAddress email);

    boolean existsByEmail(EmailAddress email);

    long countActiveByTenantIdAndRole(UUID tenantId, RoleName role);
}