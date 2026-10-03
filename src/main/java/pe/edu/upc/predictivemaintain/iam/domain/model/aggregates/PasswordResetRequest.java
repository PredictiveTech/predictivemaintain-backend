package pe.edu.upc.predictivemaintain.iam.domain.model.aggregates;

import pe.edu.upc.predictivemaintain.shared.domain.exceptions.DomainValidationException;
import pe.edu.upc.predictivemaintain.shared.domain.model.aggregates.AbstractDomainAggregateRoot;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Temporary, single-use request to recover a password. Only the hash of the
 * token is kept: the original token travels in the email link and is never stored.
 */
public class PasswordResetRequest extends AbstractDomainAggregateRoot {

    private final UUID id;
    private final UUID tenantId;
    private final UUID userId;
    private final String tokenHash;
    private final Instant expiresAt;
    private Instant usedAt;

    private PasswordResetRequest(UUID id, UUID tenantId, UUID userId, String tokenHash,
                                 Instant expiresAt, Instant usedAt) {
        this.id = Objects.requireNonNull(id);
        this.tenantId = Objects.requireNonNull(tenantId);
        this.userId = Objects.requireNonNull(userId);
        this.tokenHash = Objects.requireNonNull(tokenHash);
        this.expiresAt = Objects.requireNonNull(expiresAt);
        this.usedAt = usedAt;
    }

    public static PasswordResetRequest create(UUID tenantId, UUID userId, String tokenHash, Instant expiresAt) {
        return new PasswordResetRequest(UUID.randomUUID(), tenantId, userId, tokenHash, expiresAt, null);
    }

    public static PasswordResetRequest restore(UUID id, UUID tenantId, UUID userId, String tokenHash,
                                               Instant expiresAt, Instant usedAt) {
        return new PasswordResetRequest(id, tenantId, userId, tokenHash, expiresAt, usedAt);
    }

    /** Valid while it has not been used and has not expired. */
    public boolean isValid(Instant at) {
        return usedAt == null && at.isBefore(expiresAt);
    }

    public void consume(Instant at) {
        if (!isValid(at)) {
            throw new DomainValidationException("validation.password-reset.token-invalid");
        }
        this.usedAt = at;
    }

    public UUID getId() {
        return id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getUsedAt() {
        return usedAt;
    }
}