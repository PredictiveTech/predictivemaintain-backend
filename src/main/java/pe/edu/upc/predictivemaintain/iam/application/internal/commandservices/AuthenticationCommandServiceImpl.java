package pe.edu.upc.predictivemaintain.iam.application.internal.commandservices;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.predictivemaintain.iam.application.commandservices.AuthenticationCommandService;
import pe.edu.upc.predictivemaintain.iam.application.commandservices.LoginResult;
import pe.edu.upc.predictivemaintain.iam.application.commandservices.RegisterCompanyAccountResult;
import pe.edu.upc.predictivemaintain.iam.application.errors.IamError;
import pe.edu.upc.predictivemaintain.iam.application.internal.support.ResetTokenSupport;
import pe.edu.upc.predictivemaintain.iam.application.outboundservices.PasswordHasher;
import pe.edu.upc.predictivemaintain.iam.application.outboundservices.PasswordResetNotifier;
import pe.edu.upc.predictivemaintain.iam.application.outboundservices.TokenIssuer;
import pe.edu.upc.predictivemaintain.iam.domain.model.aggregates.PasswordResetRequest;
import pe.edu.upc.predictivemaintain.iam.domain.model.aggregates.UserAccount;
import pe.edu.upc.predictivemaintain.iam.domain.model.commands.LoginCommand;
import pe.edu.upc.predictivemaintain.iam.domain.model.commands.RegisterCompanyAccountCommand;
import pe.edu.upc.predictivemaintain.iam.domain.model.commands.RequestPasswordResetCommand;
import pe.edu.upc.predictivemaintain.iam.domain.model.commands.ResetPasswordCommand;
import pe.edu.upc.predictivemaintain.iam.domain.model.valueobjects.DisplayName;
import pe.edu.upc.predictivemaintain.iam.domain.model.valueobjects.EmailAddress;
import pe.edu.upc.predictivemaintain.iam.domain.model.valueobjects.RoleName;
import pe.edu.upc.predictivemaintain.iam.domain.repositories.PasswordResetRequestRepository;
import pe.edu.upc.predictivemaintain.iam.domain.repositories.UserAccountRepository;
import pe.edu.upc.predictivemaintain.iam.domain.services.PasswordPolicy;
import pe.edu.upc.predictivemaintain.shared.application.errors.ApplicationException;
import pe.edu.upc.predictivemaintain.shared.domain.exceptions.DomainValidationException;
import pe.edu.upc.predictivemaintain.subscription.interfaces.acl.SubscriptionContextFacade;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class AuthenticationCommandServiceImpl implements AuthenticationCommandService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordResetRequestRepository passwordResetRequestRepository;
    private final PasswordHasher passwordHasher;
    private final TokenIssuer tokenIssuer;
    private final PasswordResetNotifier passwordResetNotifier;
    private final SubscriptionContextFacade subscriptionContextFacade;
    private final Clock clock;
    private final long resetExpirationMinutes;

    /** Hash used to spend the same time when the email does not exist (hides which emails are registered). */
    private final String dummyHash;

    public AuthenticationCommandServiceImpl(UserAccountRepository userAccountRepository,
                                            PasswordResetRequestRepository passwordResetRequestRepository,
                                            PasswordHasher passwordHasher,
                                            TokenIssuer tokenIssuer,
                                            PasswordResetNotifier passwordResetNotifier,
                                            SubscriptionContextFacade subscriptionContextFacade,
                                            Clock clock,
                                            @Value("${app.password-reset.expiration-minutes:30}") long resetExpirationMinutes) {
        this.userAccountRepository = userAccountRepository;
        this.passwordResetRequestRepository = passwordResetRequestRepository;
        this.passwordHasher = passwordHasher;
        this.tokenIssuer = tokenIssuer;
        this.passwordResetNotifier = passwordResetNotifier;
        this.subscriptionContextFacade = subscriptionContextFacade;
        this.clock = clock;
        this.resetExpirationMinutes = resetExpirationMinutes;
        this.dummyHash = passwordHasher.hash("timing-protection-password");
    }

    @Override
    @Transactional
    public RegisterCompanyAccountResult handle(RegisterCompanyAccountCommand command) {
        EmailAddress email = new EmailAddress(command.email());
        PasswordPolicy.validate(command.password());

        // Idempotent replay: same registrationId, same email and same password = same result.
        Optional<UUID> previousTenant =
                subscriptionContextFacade.findTenantIdByRegistrationId(command.registrationId());
        if (previousTenant.isPresent()) {
            return userAccountRepository.findByEmail(email)
                    .filter(user -> user.getTenantId().equals(previousTenant.get()))
                    .filter(user -> passwordHasher.matches(command.password(), user.getPasswordHash()))
                    .map(user -> new RegisterCompanyAccountResult(user.getTenantId(), user.getId(), false))
                    .orElseThrow(() -> new ApplicationException(IamError.REGISTRATION_ID_ALREADY_USED));
        }

        if (userAccountRepository.existsByEmail(email)) {
            throw new ApplicationException(IamError.ACCOUNT_ALREADY_EXISTS);
        }

        UUID tenantId = subscriptionContextFacade.provisionCompany(command.companyName(), command.registrationId());
        UserAccount manager = UserAccount.register(
                tenantId,
                email,
                initialDisplayName(email),
                passwordHasher.hash(command.password()),
                Set.of(RoleName.MAINTENANCE_MANAGER));
        UserAccount saved = userAccountRepository.save(manager);
        return new RegisterCompanyAccountResult(tenantId, saved.getId(), true);
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResult handle(LoginCommand command) {
        Optional<UserAccount> candidate = findByLoginEmail(command.email());
        String hashToCheck = candidate.map(UserAccount::getPasswordHash).orElse(dummyHash);
        boolean passwordMatches = passwordHasher.matches(command.password(), hashToCheck);

        // One generic error for "unknown email" and "wrong password" (US-21).
        if (candidate.isEmpty() || !passwordMatches) {
            throw new ApplicationException(IamError.INVALID_CREDENTIALS);
        }
        UserAccount user = candidate.get();
        if (!user.isActive()) {
            throw new ApplicationException(IamError.ACCOUNT_DISABLED);
        }
        return new LoginResult(user, tokenIssuer.issue(user));
    }

    @Override
    @Transactional
    public void handle(RequestPasswordResetCommand command) {
        EmailAddress email = new EmailAddress(command.email());
        // US-22 asks to tell the user when the email is not registered.
        UserAccount user = userAccountRepository.findByEmail(email)
                .filter(UserAccount::isActive)
                .orElseThrow(() -> new ApplicationException(IamError.ACCOUNT_NOT_FOUND));

        String token = ResetTokenSupport.generate();
        Instant expiresAt = clock.instant().plus(Duration.ofMinutes(resetExpirationMinutes));
        passwordResetRequestRepository.save(
                PasswordResetRequest.create(user.getTenantId(), user.getId(), ResetTokenSupport.hash(token), expiresAt));
        passwordResetNotifier.sendResetLink(email, token);
    }

    @Override
    @Transactional
    public void handle(ResetPasswordCommand command) {
        PasswordPolicy.validate(command.newPassword());
        Instant now = clock.instant();

        PasswordResetRequest request = passwordResetRequestRepository
                .findByTokenHash(ResetTokenSupport.hash(command.token()))
                .filter(candidate -> candidate.isValid(now))
                .orElseThrow(() -> new ApplicationException(IamError.RESET_TOKEN_INVALID));
        UserAccount user = userAccountRepository.findById(request.getUserId())
                .filter(UserAccount::isActive)
                .orElseThrow(() -> new ApplicationException(IamError.RESET_TOKEN_INVALID));

        // Both changes are saved in the same transaction: the token cannot be reused.
        request.consume(now);
        user.changePassword(passwordHasher.hash(command.newPassword()));
        passwordResetRequestRepository.save(request);
        userAccountRepository.save(user);
    }

    private Optional<UserAccount> findByLoginEmail(String rawEmail) {
        try {
            return userAccountRepository.findByEmail(new EmailAddress(rawEmail));
        } catch (DomainValidationException ex) {
            return Optional.empty();
        }
    }

    /** The registration form has no name field, so the first display name is the part before the @. */
    private static DisplayName initialDisplayName(EmailAddress email) {
        String value = email.value();
        return new DisplayName(value.substring(0, value.indexOf('@')));
    }
}