package pe.edu.upc.predictivemaintain.notifications.interfaces.rest.transform;

import pe.edu.upc.predictivemaintain.notifications.domain.model.aggregates.DeviceToken;
import pe.edu.upc.predictivemaintain.notifications.domain.model.aggregates.NotificationDelivery;
import pe.edu.upc.predictivemaintain.notifications.domain.model.aggregates.NotificationRule;
import pe.edu.upc.predictivemaintain.notifications.interfaces.rest.resources.DeviceResource;
import pe.edu.upc.predictivemaintain.notifications.interfaces.rest.resources.NotificationResource;
import pe.edu.upc.predictivemaintain.notifications.interfaces.rest.resources.NotificationRuleResource;

public final class NotificationResourceAssembler {

    private NotificationResourceAssembler() {
    }

    public static DeviceResource toResource(DeviceToken deviceToken) {
        return new DeviceResource(deviceToken.getId(), deviceToken.getPlatform(), deviceToken.getRegisteredAt());
    }

    public static NotificationRuleResource toResource(NotificationRule rule) {
        return new NotificationRuleResource(rule.getId(), rule.getUserId(),
                rule.appliesToAllTypes() ? null : rule.getAssetType(), rule.getChannel());
    }

    public static NotificationResource toResource(NotificationDelivery delivery) {
        return new NotificationResource(delivery.id(), delivery.type(), delivery.channel(), delivery.title(),
                delivery.body(), delivery.subjectId(), delivery.status(), delivery.detail(), delivery.attemptedAt());
    }
}