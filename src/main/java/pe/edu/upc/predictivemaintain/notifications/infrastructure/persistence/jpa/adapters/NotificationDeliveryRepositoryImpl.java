package pe.edu.upc.predictivemaintain.notifications.infrastructure.persistence.jpa.adapters;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import pe.edu.upc.predictivemaintain.notifications.domain.model.aggregates.NotificationDelivery;
import pe.edu.upc.predictivemaintain.notifications.domain.model.valueobjects.NotificationChannel;
import pe.edu.upc.predictivemaintain.notifications.domain.repositories.NotificationDeliveryRepository;
import pe.edu.upc.predictivemaintain.notifications.infrastructure.persistence.jpa.assemblers.NotificationsPersistenceAssembler;
import pe.edu.upc.predictivemaintain.notifications.infrastructure.persistence.jpa.entities.NotificationDeliveryPersistenceEntity;
import pe.edu.upc.predictivemaintain.notifications.infrastructure.persistence.jpa.repositories.NotificationDeliveryPersistenceRepository;
import pe.edu.upc.predictivemaintain.shared.domain.model.valueobjects.PageQuery;
import pe.edu.upc.predictivemaintain.shared.domain.model.valueobjects.PagedResult;

import java.util.UUID;

@Repository
public class NotificationDeliveryRepositoryImpl implements NotificationDeliveryRepository {

    private final NotificationDeliveryPersistenceRepository jpaRepository;

    public NotificationDeliveryRepositoryImpl(NotificationDeliveryPersistenceRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public NotificationDelivery save(NotificationDelivery delivery) {
        return NotificationsPersistenceAssembler.toDomain(
                jpaRepository.save(NotificationsPersistenceAssembler.toEntity(delivery)));
    }

    @Override
    public boolean exists(UUID tenantId, UUID userId, NotificationChannel channel, String dedupeKey) {
        return jpaRepository.existsByTenantIdAndUserIdAndChannelAndDedupeKey(tenantId, userId, channel, dedupeKey);
    }

    @Override
    public PagedResult<NotificationDelivery> findByUser(UUID tenantId, UUID userId, PageQuery pageQuery) {
        Page<NotificationDeliveryPersistenceEntity> page = jpaRepository.findByTenantIdAndUserId(tenantId, userId,
                PageRequest.of(pageQuery.page(), pageQuery.size(), Sort.by(Sort.Direction.DESC, "attemptedAt")));
        return new PagedResult<>(
                page.getContent().stream().map(NotificationsPersistenceAssembler::toDomain).toList(),
                page.getTotalElements(),
                pageQuery.page(),
                pageQuery.size());
    }
}