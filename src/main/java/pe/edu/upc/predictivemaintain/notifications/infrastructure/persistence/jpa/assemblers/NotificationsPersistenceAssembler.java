package pe.edu.upc.predictivemaintain.notifications.infrastructure.persistence.jpa.assemblers;

import pe.edu.upc.predictivemaintain.notifications.domain.model.aggregates.DeviceToken;
import pe.edu.upc.predictivemaintain.notifications.domain.model.aggregates.NotificationDelivery;
import pe.edu.upc.predictivemaintain.notifications.domain.model.aggregates.NotificationRule;
import pe.edu.upc.predictivemaintain.notifications.infrastructure.persistence.jpa.entities.DeviceTokenPersistenceEntity;
import pe.edu.upc.predictivemaintain.notifications.infrastructure.persistence.jpa.entities.NotificationDeliveryPersistenceEntity;
import pe.edu.upc.predictivemaintain.notifications.infrastructure.persistence.jpa.entities.NotificationRulePersistenceEntity;

/**
 * Translations between the three aggregates of the context and their tables.
 */
public final class NotificationsPersistenceAssembler {

    private NotificationsPersistenceAssembler() {
    }

    // ---- device tokens
    public static DeviceToken toDomain(DeviceTokenPersistenceEntity entity) {
        return DeviceToken.restore(entity.getId(), entity.getTenantId(), entity.getUserId(), entity.getToken(),
                entity.getPlatform(), entity.getRegisteredAt(), entity.getLastSeenAt());
    }

    public static void copyToEntity(DeviceToken deviceToken, DeviceTokenPersistenceEntity entity) {
        entity.setId(deviceToken.getId());
        entity.setTenantId(deviceToken.getTenantId());
        entity.setUserId(deviceToken.getUserId());
        entity.setToken(deviceToken.getToken());
        entity.setPlatform(deviceToken.getPlatform());
        entity.setRegisteredAt(deviceToken.getRegisteredAt());
        entity.setLastSeenAt(deviceToken.getLastSeenAt());
    }

    // ---- rules
    public static NotificationRule toDomain(NotificationRulePersistenceEntity entity) {
        return NotificationRule.restore(entity.getId(), entity.getTenantId(), entity.getUserId(),
                entity.getAssetType(), entity.getChannel());
    }

    public static NotificationRulePersistenceEntity toEntity(NotificationRule rule) {
        NotificationRulePersistenceEntity entity = new NotificationRulePersistenceEntity();
        entity.setId(rule.getId());
        entity.setTenantId(rule.getTenantId());
        entity.setUserId(rule.getUserId());
        entity.setAssetType(rule.getAssetType());
        entity.setChannel(rule.getChannel());
        return entity;
    }

    // ---- deliveries
    public static NotificationDelivery toDomain(NotificationDeliveryPersistenceEntity entity) {
        return new NotificationDelivery(entity.getId(), entity.getTenantId(), entity.getUserId(), entity.getChannel(),
                entity.getType(), entity.getSubjectId(), entity.getDedupeKey(), entity.getTitle(), entity.getBody(),
                entity.getStatus(), entity.getDetail(), entity.getAttemptedAt());
    }

    public static NotificationDeliveryPersistenceEntity toEntity(NotificationDelivery delivery) {
        NotificationDeliveryPersistenceEntity entity = new NotificationDeliveryPersistenceEntity();
        entity.setId(delivery.id());
        entity.setTenantId(delivery.tenantId());
        entity.setUserId(delivery.userId());
        entity.setChannel(delivery.channel());
        entity.setType(delivery.type());
        entity.setSubjectId(delivery.subjectId());
        entity.setDedupeKey(delivery.dedupeKey());
        entity.setTitle(delivery.title());
        entity.setBody(delivery.body());
        entity.setStatus(delivery.status());
        entity.setDetail(delivery.detail());
        entity.setAttemptedAt(delivery.attemptedAt());
        return entity;
    }
}