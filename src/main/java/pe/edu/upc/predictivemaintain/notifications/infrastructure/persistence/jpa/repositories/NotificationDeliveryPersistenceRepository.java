package pe.edu.upc.predictivemaintain.notifications.infrastructure.persistence.jpa.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.predictivemaintain.notifications.domain.model.valueobjects.NotificationChannel;
import pe.edu.upc.predictivemaintain.notifications.infrastructure.persistence.jpa.entities.NotificationDeliveryPersistenceEntity;

import java.util.UUID;

public interface NotificationDeliveryPersistenceRepository
        extends JpaRepository<NotificationDeliveryPersistenceEntity, UUID> {

    boolean existsByTenantIdAndUserIdAndChannelAndDedupeKey(UUID tenantId, UUID userId, NotificationChannel channel,
                                                            String dedupeKey);

    Page<NotificationDeliveryPersistenceEntity> findByTenantIdAndUserId(UUID tenantId, UUID userId, Pageable pageable);
}