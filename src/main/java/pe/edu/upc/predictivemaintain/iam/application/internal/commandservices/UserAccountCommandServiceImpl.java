package pe.edu.upc.predictivemaintain.iam.application.internal.commandservices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.predictivemaintain.iam.application.commandservices.UserAccountCommandService;
import pe.edu.upc.predictivemaintain.iam.application.errors.IamError;
import pe.edu.upc.predictivemaintain.iam.application.outboundservices.PasswordHasher;
import pe.edu.upc.predictivemaintain.iam.domain.model.aggregates.UserAccount;
import pe.edu.upc.predictivemaintain.iam.domain.model.commands.ChangeUserRolesCommand;
import pe.edu.upc.predictivemaintain.iam.domain.model.commands.CreateUserCommand;
import pe.edu.upc.predictivemaintain.iam.domain.model.commands.DeactivateUserCommand;
import pe.edu.upc.predictivemaintain.iam.domain.model.commands.UpdateProfileCommand;
import pe.edu.upc.predictivemaintain.iam.domain.model.valueobjects.DisplayName;
import pe.edu.upc.predictivemaintain.iam.domain.model.valueobjects.EmailAddress;
import pe.edu.upc.predictivemaintain.iam.domain.model.valueobjects.RoleName;
import pe.edu.upc.predictivemaintain.iam.domain.repositories.UserAccountRepository;
import pe.edu.upc.predictivemaintain.iam.domain.services.PasswordPolicy;
import pe.edu.upc.predictivemaintain.shared.application.errors.ApplicationException;

import java.util.UUID;

@Service
public class UserAccountCommandServiceImpl implements UserAccountCommandService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordHasher passwordHasher;

    public UserAccountCommandServiceImpl(UserAccountRepository userAccountRepository,
                                         PasswordHasher passwordHasher) {
        this.userAccountRepository = userAccountRepository;
        this.passwordHasher = passwordHasher;
    }

    @Override
    @Transactional
    public UserAccount handle(UpdateProfileCommand command) {
        UserAccount user = userAccountRepository.findById(command.userId())
                .orElseThrow(() -> new ApplicationException(IamError.USER_NOT_FOUND));
        user.updateDisplayName(new DisplayName(command.displayName()));
        return userAccountRepository.save(user);
    }

    @Override
    @Transactional
    public UserAccount handle(CreateUserCommand command) {
        EmailAddress email = new EmailAddress(command.email());
        PasswordPolicy.validate(command.initialPassword());
        if (userAccountRepository.existsByEmail(email)) {
            throw new ApplicationException(IamError.ACCOUNT_ALREADY_EXISTS);
        }
        UserAccount user = UserAccount.register(
                command.tenantId(),
                email,
                new DisplayName(command.displayName()),
                passwordHasher.hash(command.initialPassword()),
                command.roles());
        return userAccountRepository.save(user);
    }

    @Override
    @Transactional
    public UserAccount handle(ChangeUserRolesCommand command) {
        UserAccount user = findInCompany(command.userId(), command.tenantId());
        boolean losesManagerRole = user.hasRole(RoleName.MAINTENANCE_MANAGER)
                && (command.roles() == null || !command.roles().contains(RoleName.MAINTENANCE_MANAGER));
        if (losesManagerRole && user.isActive()) {
            ensureAnotherActiveManager(command.tenantId());
        }
        user.replaceRoles(command.roles());
        return userAccountRepository.save(user);
    }

    @Override
    @Transactional
    public UserAccount handle(DeactivateUserCommand command) {
        UserAccount user = findInCompany(command.userId(), command.tenantId());
        if (user.hasRole(RoleName.MAINTENANCE_MANAGER) && user.isActive()) {
            ensureAnotherActiveManager(command.tenantId());
        }
        user.deactivate();
        return userAccountRepository.save(user);
    }

    /** Looks the user up inside the manager's company: other companies' users look like "not found". */
    private UserAccount findInCompany(UUID userId, UUID tenantId) {
        return userAccountRepository.findByIdAndTenantId(userId, tenantId)
                .orElseThrow(() -> new ApplicationException(IamError.USER_NOT_FOUND));
    }

    /** A company without an active manager would have nobody able to administer it. */
    private void ensureAnotherActiveManager(UUID tenantId) {
        if (userAccountRepository.countActiveByTenantIdAndRole(tenantId, RoleName.MAINTENANCE_MANAGER) <= 1) {
            throw new ApplicationException(IamError.LAST_MANAGER_REQUIRED);
        }
    }
}