package pe.edu.upc.predictivemaintain.notifications.application.queryservices;

import pe.edu.upc.predictivemaintain.notifications.domain.model.aggregates.NotificationDelivery;
import pe.edu.upc.predictivemaintain.notifications.domain.model.aggregates.NotificationRule;
import pe.edu.upc.predictivemaintain.notifications.domain.model.queries.GetMyNotificationsQuery;
import pe.edu.upc.predictivemaintain.notifications.domain.model.queries.GetNotificationRulesQuery;
import pe.edu.upc.predictivemaintain.shared.domain.model.valueobjects.PagedResult;

import java.util.List;

public interface NotificationQueryService {

    List<NotificationRule> handle(GetNotificationRulesQuery query);

    PagedResult<NotificationDelivery> handle(GetMyNotificationsQuery query);
}