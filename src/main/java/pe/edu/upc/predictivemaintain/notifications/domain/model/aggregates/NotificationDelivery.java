package pe.edu.upc.predictivemaintain.notifications.domain.model.aggregates;

import pe.edu.upc.predictivemaintain.notifications.domain.model.valueobjects.DeliveryStatus;
import pe.edu.upc.predictivemaintain.notifications.domain.model.valueobjects.NotificationChannel;
import pe.edu.upc.predictivemaintain.notifications.domain.model.valueobjects.NotificationType;

import java.time.Instant;
import java.util.UUID;

/**
 * The record of one notification attempt to one person through one channel (TS-08: "registra el envío").
 *
 * @param subjectId definitive reference of what it is about (the alert, the subscription)
 * @param dedupeKey identifies the event; with the person and the channel it makes a notification unique,
 *                  so the same event is never notified twice to the same person
 * @param detail    why it failed or was skipped; null when it was sent
 */
public record NotificationDelivery(UUID id, UUID tenantId, UUID userId, NotificationChannel channel,
                                   NotificationType type, UUID subjectId, String dedupeKey, String title,
                                   String body, DeliveryStatus status, String detail, Instant attemptedAt) {

    public static NotificationDelivery create(UUID tenantId, UUID userId, NotificationChannel channel,
                                              NotificationType type, UUID subjectId, String dedupeKey,
                                              String title, String body, DeliveryStatus status, String detail,
                                              Instant attemptedAt) {
        return new NotificationDelivery(UUID.randomUUID(), tenantId, userId, channel, type, subjectId, dedupeKey,
                cut(title, 150), cut(body, 500), status, cut(detail, 300), attemptedAt);
    }

    private static String cut(String text, int max) {
        if (text == null) {
            return null;
        }
        return text.length() <= max ? text : text.substring(0, max);
    }
}