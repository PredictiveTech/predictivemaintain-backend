package pe.edu.upc.predictivemaintain.notifications.domain.repositories;

import pe.edu.upc.predictivemaintain.notifications.domain.model.aggregates.NotificationRule;
import pe.edu.upc.predictivemaintain.notifications.domain.model.valueobjects.NotificationChannel;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRuleRepository {

    NotificationRule save(NotificationRule rule);

    Optional<NotificationRule> findByIdAndTenantId(UUID id, UUID tenantId);

    List<NotificationRule> findByTenantId(UUID tenantId);

    /** Rules for every type of asset plus the rules for that type. The type is compared ignoring case. */
    List<NotificationRule> findMatching(UUID tenantId, String assetType);

    boolean exists(UUID tenantId, UUID userId, String assetType, NotificationChannel channel);

    void delete(UUID id);
}