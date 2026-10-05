package pe.edu.upc.predictivemaintain.notifications.domain.model.aggregates;

import pe.edu.upc.predictivemaintain.notifications.domain.model.valueobjects.NotificationChannel;
import pe.edu.upc.predictivemaintain.shared.domain.exceptions.DomainValidationException;
import pe.edu.upc.predictivemaintain.shared.domain.model.aggregates.AbstractDomainAggregateRoot;

import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

/**
 * "This person receives the alerts of this kind of asset through this channel" (US-06).
 * The asset type "*" means every type.
 */
public class NotificationRule extends AbstractDomainAggregateRoot {

    /** Stands for "every type of asset". It is stored as text, so the uniqueness of rules works in the database. */
    public static final String ANY_TYPE = "*";

    private static final int MAX_TYPE_LENGTH = 60;

    private final UUID id;
    private final UUID tenantId;
    private final UUID userId;
    private final String assetType;
    private final NotificationChannel channel;

    private NotificationRule(UUID id, UUID tenantId, UUID userId, String assetType, NotificationChannel channel) {
        if (channel == null) {
            throw new DomainValidationException("validation.notification-rule.channel-required");
        }
        this.id = Objects.requireNonNull(id);
        this.tenantId = Objects.requireNonNull(tenantId);
        this.userId = Objects.requireNonNull(userId);
        this.assetType = normalize(assetType);
        this.channel = channel;
    }

    public static NotificationRule create(UUID tenantId, UUID userId, String assetType, NotificationChannel channel) {
        return new NotificationRule(UUID.randomUUID(), tenantId, userId, assetType, channel);
    }

    public static NotificationRule restore(UUID id, UUID tenantId, UUID userId, String assetType,
                                           NotificationChannel channel) {
        return new NotificationRule(id, tenantId, userId, assetType, channel);
    }

    /** Blank means every type; otherwise the type in capitals, because asset types are compared ignoring case. */
    public static String normalize(String assetType) {
        if (assetType == null || assetType.isBlank()) {
            return ANY_TYPE;
        }
        String normalized = assetType.trim().toUpperCase(Locale.ROOT);
        if (normalized.length() > MAX_TYPE_LENGTH) {
            throw new DomainValidationException("validation.notification-rule.asset-type-invalid");
        }
        return normalized;
    }

    public boolean appliesToAllTypes() {
        return ANY_TYPE.equals(assetType);
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

    public String getAssetType() {
        return assetType;
    }

    public NotificationChannel getChannel() {
        return channel;
    }
}