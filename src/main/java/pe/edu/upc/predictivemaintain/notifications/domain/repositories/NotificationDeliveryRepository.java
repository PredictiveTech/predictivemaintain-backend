package pe.edu.upc.predictivemaintain.notifications.domain.repositories;

import pe.edu.upc.predictivemaintain.notifications.domain.model.aggregates.NotificationDelivery;
import pe.edu.upc.predictivemaintain.notifications.domain.model.valueobjects.NotificationChannel;
import pe.edu.upc.predictivemaintain.shared.domain.model.valueobjects.PageQuery;
import pe.edu.upc.predictivemaintain.shared.domain.model.valueobjects.PagedResult;

import java.util.UUID;

public interface NotificationDeliveryRepository {

    NotificationDelivery save(NotificationDelivery delivery);

    boolean exists(UUID tenantId, UUID userId, NotificationChannel channel, String dedupeKey);

    /** The notifications of one person, newest first. */
    PagedResult<NotificationDelivery> findByUser(UUID tenantId, UUID userId, PageQuery page);
}