package pe.edu.upc.predictivemaintain.telemetry.infrastructure.persistence.jpa.adapters;

import org.springframework.stereotype.Repository;
import pe.edu.upc.predictivemaintain.shared.domain.services.DomainEventPublisher;
import pe.edu.upc.predictivemaintain.telemetry.domain.model.aggregates.ThresholdRule;
import pe.edu.upc.predictivemaintain.telemetry.domain.repositories.ThresholdRuleRepository;
import pe.edu.upc.predictivemaintain.telemetry.infrastructure.persistence.jpa.assemblers.ThresholdRulePersistenceAssembler;
import pe.edu.upc.predictivemaintain.telemetry.infrastructure.persistence.jpa.entities.ThresholdRulePersistenceEntity;
import pe.edu.upc.predictivemaintain.telemetry.infrastructure.persistence.jpa.repositories.ThresholdRulePersistenceRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ThresholdRuleRepositoryImpl implements ThresholdRuleRepository {

    private final ThresholdRulePersistenceRepository jpaRepository;
    private final DomainEventPublisher eventPublisher;

    public ThresholdRuleRepositoryImpl(ThresholdRulePersistenceRepository jpaRepository,
                                       DomainEventPublisher eventPublisher) {
        this.jpaRepository = jpaRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public ThresholdRule save(ThresholdRule rule) {
        ThresholdRulePersistenceEntity entity = jpaRepository.findById(rule.getId())
                .orElseGet(ThresholdRulePersistenceEntity::new);
        ThresholdRulePersistenceAssembler.copyToEntity(rule, entity);
        ThresholdRulePersistenceEntity saved = jpaRepository.save(entity);
        eventPublisher.publishAll(rule);
        return ThresholdRulePersistenceAssembler.toDomain(saved);
    }

    @Override
    public Optional<ThresholdRule> findBySensorId(UUID tenantId, UUID sensorId) {
        return jpaRepository.findByTenantIdAndSensorId(tenantId, sensorId)
                .map(ThresholdRulePersistenceAssembler::toDomain);
    }

    @Override
    public List<ThresholdRule> findBySensorIds(UUID tenantId, Collection<UUID> sensorIds) {
        if (sensorIds.isEmpty()) {
            return List.of();
        }
        return jpaRepository.findByTenantIdAndSensorIdIn(tenantId, sensorIds).stream()
                .map(ThresholdRulePersistenceAssembler::toDomain)
                .toList();
    }
}