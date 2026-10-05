package pe.edu.upc.predictivemaintain.iam.domain.model.aggregates;

import pe.edu.upc.predictivemaintain.iam.domain.model.valueobjects.DisplayName;
import pe.edu.upc.predictivemaintain.iam.domain.model.valueobjects.EmailAddress;
import pe.edu.upc.predictivemaintain.iam.domain.model.valueobjects.RoleName;
import pe.edu.upc.predictivemaintain.shared.domain.exceptions.DomainValidationException;
import pe.edu.upc.predictivemaintain.shared.domain.model.aggregates.AbstractDomainAggregateRoot;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Identity of a person inside a company (tenant). It always has at least one role
 * and only stores the password hash, never the password.
 */
public class UserAccount extends AbstractDomainAggregateRoot {

    private final UUID id;
    private final UUID tenantId;
    private final EmailAddress email;
    private DisplayName displayName;
    private String passwordHash;
    private boolean active;
    private final Set<RoleName> roles = EnumSet.noneOf(RoleName.class);

    private UserAccount(UUID id, UUID tenantId, EmailAddress email, DisplayName displayName,
                        String passwordHash, boolean active, Set<RoleName> roles) {
        requireRoles(roles);
        this.id = Objects.requireNonNull(id);
        this.tenantId = Objects.requireNonNull(tenantId);
        this.email = Objects.requireNonNull(email);
        this.displayName = Objects.requireNonNull(displayName);
        this.passwordHash = Objects.requireNonNull(passwordHash);
        this.active = active;
        this.roles.addAll(roles);
    }

    public static UserAccount register(UUID tenantId, EmailAddress email, DisplayName displayName,
                                       String passwordHash, Set<RoleName> roles) {
        return new UserAccount(UUID.randomUUID(), tenantId, email, displayName, passwordHash, true, roles);
    }

    public static UserAccount restore(UUID id, UUID tenantId, EmailAddress email, DisplayName displayName,
                                      String passwordHash, boolean active, Set<RoleName> roles) {
        return new UserAccount(id, tenantId, email, displayName, passwordHash, active, roles);
    }

    public void deactivate() {
        this.active = false;
    }

    public void changePassword(String newPasswordHash) {
        this.passwordHash = Objects.requireNonNull(newPasswordHash);
    }

    public void updateDisplayName(DisplayName newDisplayName) {
        this.displayName = Objects.requireNonNull(newDisplayName);
    }

    /** Replaces all roles. A user must always keep at least one. */
    public void replaceRoles(Set<RoleName> newRoles) {
        requireRoles(newRoles);
        Set<RoleName> copy = EnumSet.copyOf(newRoles);
        roles.clear();
        roles.addAll(copy);
    }

    public boolean hasRole(RoleName role) {
        return roles.contains(role);
    }

    private static void requireRoles(Set<RoleName> candidate) {
        if (candidate == null || candidate.isEmpty()) {
            throw new DomainValidationException("validation.roles.required");
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public EmailAddress getEmail() {
        return email;
    }

    public DisplayName getDisplayName() {
        return displayName;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public boolean isActive() {
        return active;
    }

    public Set<RoleName> getRoles() {
        return Collections.unmodifiableSet(roles);
    }
}