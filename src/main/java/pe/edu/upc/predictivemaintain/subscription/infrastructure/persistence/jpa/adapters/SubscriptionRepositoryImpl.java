package pe.edu.upc.predictivemaintain.subscription.infrastructure.persistence.jpa.adapters;

import org.springframework.stereotype.Repository;
import pe.edu.upc.predictivemaintain.shared.domain.services.DomainEventPublisher;
import pe.edu.upc.predictivemaintain.subscription.domain.model.aggregates.Subscription;
import pe.edu.upc.predictivemaintain.subscription.domain.repositories.SubscriptionRepository;
import pe.edu.upc.predictivemaintain.subscription.infrastructure.persistence.jpa.assemblers.SubscriptionPersistenceAssembler;
import pe.edu.upc.predictivemaintain.subscription.infrastructure.persistence.jpa.entities.SubscriptionPersistenceEntity;
import pe.edu.upc.predictivemaintain.subscription.infrastructure.persistence.jpa.repositories.SubscriptionPersistenceRepository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class SubscriptionRepositoryImpl implements SubscriptionRepository {

    private final SubscriptionPersistenceRepository jpaRepository;
    private final DomainEventPublisher eventPublisher;

    public SubscriptionRepositoryImpl(SubscriptionPersistenceRepository jpaRepository,
                                      DomainEventPublisher eventPublisher) {
        this.jpaRepository = jpaRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Subscription save(Subscription subscription) {
        SubscriptionPersistenceEntity entity = jpaRepository.findById(subscription.getId())
                .orElseGet(SubscriptionPersistenceEntity::new);
        SubscriptionPersistenceAssembler.copyToEntity(subscription, entity);
        SubscriptionPersistenceEntity saved = jpaRepository.save(entity);
        eventPublisher.publishAll(subscription);
        return SubscriptionPersistenceAssembler.toDomain(saved);
    }

    @Override
    public Optional<Subscription> findCurrentByTenantId(UUID tenantId) {
        return jpaRepository.findFirstByTenantIdOrderByStartsAtDesc(tenantId)
                .map(SubscriptionPersistenceAssembler::toDomain);
    }
}