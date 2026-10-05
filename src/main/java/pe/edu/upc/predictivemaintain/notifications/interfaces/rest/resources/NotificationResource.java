package pe.edu.upc.predictivemaintain.notifications.interfaces.rest.resources;

import pe.edu.upc.predictivemaintain.notifications.domain.model.valueobjects.DeliveryStatus;
import pe.edu.upc.predictivemaintain.notifications.domain.model.valueobjects.NotificationChannel;
import pe.edu.upc.predictivemaintain.notifications.domain.model.valueobjects.NotificationType;

import java.time.Instant;
import java.util.UUID;

/**
 * One notification sent (or tried) to the logged-in user.
 *
 * @param subjectId id of the alert or of the subscription it is about
 * @param detail    why it failed or was skipped; null when it was sent
 */
public record NotificationResource(UUID id, NotificationType type, NotificationChannel channel, String title,
                                   String body, UUID subjectId, DeliveryStatus status, String detail,
                                   Instant attemptedAt) {
}