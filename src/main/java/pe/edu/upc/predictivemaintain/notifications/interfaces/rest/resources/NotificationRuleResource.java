package pe.edu.upc.predictivemaintain.notifications.interfaces.rest.resources;

import pe.edu.upc.predictivemaintain.notifications.domain.model.valueobjects.NotificationChannel;

import java.util.UUID;

/**
 * @param assetType null when the rule applies to every kind of asset
 */
public record NotificationRuleResource(UUID id, UUID userId, String assetType, NotificationChannel channel) {
}