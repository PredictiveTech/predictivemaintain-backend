package pe.edu.upc.predictivemaintain.iam.domain.repositories;

import pe.edu.upc.predictivemaintain.iam.domain.model.aggregates.UserAccount;
import pe.edu.upc.predictivemaintain.iam.domain.model.valueobjects.EmailAddress;
import pe.edu.upc.predictivemaintain.iam.domain.model.valueobjects.RoleName;
import pe.edu.upc.predictivemaintain.shared.domain.model.valueobjects.PageQuery;
import pe.edu.upc.predictivemaintain.shared.domain.model.valueobjects.PagedResult;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

public interface UserAccountRepository {

    UserAccount save(UserAccount user);

    Optional<UserAccount> findById(UUID id);

    /** Lookup limited to one company: use it for any operation on behalf of a manager. */
    Optional<UserAccount> findByIdAndTenantId(UUID id, UUID tenantId);

    Optional<UserAccount> findByEmail(EmailAddress email);

    boolean existsByEmail(EmailAddress email);

    long countActiveByTenantIdAndRole(UUID tenantId, RoleName role);

    /** Active users of a company that have a role. */
    List<UserAccount> findActiveByTenantIdAndRole(UUID tenantId, RoleName role);

    /**
     * The users of one company, ordered by name. Both filters are optional.
     *
     * @param role   only users with this role; null for any
     * @param active true for active users only, false for deactivated only; null for both
     */
    PagedResult<UserAccount> search(UUID tenantId, RoleName role, Boolean active, PageQuery page);
}