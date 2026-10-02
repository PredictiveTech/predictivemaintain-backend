package pe.edu.upc.predictivemaintain.subscription.infrastructure.persistence.jpa.adapters;

import org.springframework.stereotype.Repository;
import pe.edu.upc.predictivemaintain.shared.domain.services.DomainEventPublisher;
import pe.edu.upc.predictivemaintain.subscription.domain.model.aggregates.SubscriptionPlan;
import pe.edu.upc.predictivemaintain.subscription.domain.repositories.SubscriptionPlanRepository;
import pe.edu.upc.predictivemaintain.subscription.infrastructure.persistence.jpa.assemblers.SubscriptionPlanPersistenceAssembler;
import pe.edu.upc.predictivemaintain.subscription.infrastructure.persistence.jpa.entities.SubscriptionPlanPersistenceEntity;
import pe.edu.upc.predictivemaintain.subscription.infrastructure.persistence.jpa.repositories.SubscriptionPlanPersistenceRepository;

import java.util.List;
import java.util.Optional;

/**
 * Adapter that implements the domain port with Spring Data JPA.
 */
@Repository
public class SubscriptionPlanRepositoryImpl implements SubscriptionPlanRepository {

    private final SubscriptionPlanPersistenceRepository jpaRepository;
    private final DomainEventPublisher eventPublisher;

    public SubscriptionPlanRepositoryImpl(SubscriptionPlanPersistenceRepository jpaRepository,
                                          DomainEventPublisher eventPublisher) {
        this.jpaRepository = jpaRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public SubscriptionPlan save(SubscriptionPlan plan) {
        SubscriptionPlanPersistenceEntity entity = jpaRepository.findById(plan.getId())
                .orElseGet(SubscriptionPlanPersistenceEntity::new);
        SubscriptionPlanPersistenceAssembler.copyToEntity(plan, entity);
        SubscriptionPlanPersistenceEntity saved = jpaRepository.save(entity);
        eventPublisher.publishAll(plan);
        return SubscriptionPlanPersistenceAssembler.toDomain(saved);
    }

    @Override
    public Optional<SubscriptionPlan> findByName(String name) {
        return jpaRepository.findByName(name).map(SubscriptionPlanPersistenceAssembler::toDomain);
    }

    @Override
    public List<SubscriptionPlan> findAllOrderedByAssetLimit() {
        return jpaRepository.findAllByOrderByAssetLimitAsc().stream()
                .map(SubscriptionPlanPersistenceAssembler::toDomain)
                .toList();
    }

    @Override
    public boolean existsByName(String name) {
        return jpaRepository.existsByName(name);
    }
}