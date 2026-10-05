package pe.edu.upc.predictivemaintain.notifications.domain.model.commands;

import pe.edu.upc.predictivemaintain.notifications.domain.model.valueobjects.NotificationChannel;

import java.util.UUID;

/**
 * @param userId    the person who will receive the notifications
 * @param assetType the kind of asset; null or blank means all of them
 */
public record CreateNotificationRuleCommand(UUID tenantId, UUID userId, String assetType, NotificationChannel channel) {
}