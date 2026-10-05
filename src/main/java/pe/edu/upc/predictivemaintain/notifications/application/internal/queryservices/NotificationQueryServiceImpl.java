package pe.edu.upc.predictivemaintain.notifications.application.internal.queryservices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.predictivemaintain.notifications.application.queryservices.NotificationQueryService;
import pe.edu.upc.predictivemaintain.notifications.domain.model.aggregates.NotificationDelivery;
import pe.edu.upc.predictivemaintain.notifications.domain.model.aggregates.NotificationRule;
import pe.edu.upc.predictivemaintain.notifications.domain.model.queries.GetMyNotificationsQuery;
import pe.edu.upc.predictivemaintain.notifications.domain.model.queries.GetNotificationRulesQuery;
import pe.edu.upc.predictivemaintain.notifications.domain.repositories.NotificationDeliveryRepository;
import pe.edu.upc.predictivemaintain.notifications.domain.repositories.NotificationRuleRepository;
import pe.edu.upc.predictivemaintain.shared.domain.model.valueobjects.PagedResult;

import java.util.List;

@Service
public class NotificationQueryServiceImpl implements NotificationQueryService {

    private final NotificationRuleRepository ruleRepository;
    private final NotificationDeliveryRepository deliveryRepository;

    public NotificationQueryServiceImpl(NotificationRuleRepository ruleRepository,
                                        NotificationDeliveryRepository deliveryRepository) {
        this.ruleRepository = ruleRepository;
        this.deliveryRepository = deliveryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationRule> handle(GetNotificationRulesQuery query) {
        return ruleRepository.findByTenantId(query.tenantId());
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResult<NotificationDelivery> handle(GetMyNotificationsQuery query) {
        return deliveryRepository.findByUser(query.tenantId(), query.userId(), query.page());
    }
}