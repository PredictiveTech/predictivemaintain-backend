package pe.edu.upc.predictivemaintain.notifications.domain.model.aggregates;

import pe.edu.upc.predictivemaintain.notifications.domain.model.valueobjects.DevicePlatform;
import pe.edu.upc.predictivemaintain.shared.domain.exceptions.DomainValidationException;
import pe.edu.upc.predictivemaintain.shared.domain.model.aggregates.AbstractDomainAggregateRoot;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * The address of one app installation in the push service (an FCM registration token). A token belongs to a
 * physical installation, so if another person logs in on the same phone the token MOVES to that person:
 * otherwise the first person would keep receiving the second one's alerts.
 */
public class DeviceToken extends AbstractDomainAggregateRoot {

    private static final int MAX_TOKEN_LENGTH = 512;

    private final UUID id;
    private UUID tenantId;
    private UUID userId;
    private final String token;
    private DevicePlatform platform;
    private final Instant registeredAt;
    private Instant lastSeenAt;

    private DeviceToken(UUID id, UUID tenantId, UUID userId, String token, DevicePlatform platform,
                        Instant registeredAt, Instant lastSeenAt) {
        if (token == null || token.isBlank() || token.trim().length() > MAX_TOKEN_LENGTH) {
            throw new DomainValidationException("validation.device.token-invalid");
        }
        if (platform == null) {
            throw new DomainValidationException("validation.device.platform-required");
        }
        this.id = Objects.requireNonNull(id);
        this.tenantId = Objects.requireNonNull(tenantId);
        this.userId = Objects.requireNonNull(userId);
        this.token = token.trim();
        this.platform = platform;
        this.registeredAt = Objects.requireNonNull(registeredAt);
        this.lastSeenAt = Objects.requireNonNull(lastSeenAt);
    }

    public static DeviceToken register(UUID tenantId, UUID userId, String token, DevicePlatform platform,
                                       Instant now) {
        return new DeviceToken(UUID.randomUUID(), tenantId, userId, token, platform, now, now);
    }

    public static DeviceToken restore(UUID id, UUID tenantId, UUID userId, String token, DevicePlatform platform,
                                      Instant registeredAt, Instant lastSeenAt) {
        return new DeviceToken(id, tenantId, userId, token, platform, registeredAt, lastSeenAt);
    }

    /** The same installation registers again (the app does it at every login): it belongs to whoever is logged in. */
    public void rebind(UUID newTenantId, UUID newUserId, DevicePlatform newPlatform, Instant now) {
        if (newPlatform == null) {
            throw new DomainValidationException("validation.device.platform-required");
        }
        this.tenantId = Objects.requireNonNull(newTenantId);
        this.userId = Objects.requireNonNull(newUserId);
        this.platform = newPlatform;
        this.lastSeenAt = Objects.requireNonNull(now);
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

    public String getToken() {
        return token;
    }

    public DevicePlatform getPlatform() {
        return platform;
    }

    public Instant getRegisteredAt() {
        return registeredAt;
    }

    public Instant getLastSeenAt() {
        return lastSeenAt;
    }
}