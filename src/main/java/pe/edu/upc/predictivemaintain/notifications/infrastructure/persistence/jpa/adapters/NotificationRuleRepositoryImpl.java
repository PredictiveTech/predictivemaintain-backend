package pe.edu.upc.predictivemaintain.notifications.infrastructure.persistence.jpa.adapters;

import org.springframework.stereotype.Repository;
import pe.edu.upc.predictivemaintain.notifications.domain.model.aggregates.NotificationRule;
import pe.edu.upc.predictivemaintain.notifications.domain.model.valueobjects.NotificationChannel;
import pe.edu.upc.predictivemaintain.notifications.domain.repositories.NotificationRuleRepository;
import pe.edu.upc.predictivemaintain.notifications.infrastructure.persistence.jpa.assemblers.NotificationsPersistenceAssembler;
import pe.edu.upc.predictivemaintain.notifications.infrastructure.persistence.jpa.repositories.NotificationRulePersistenceRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class NotificationRuleRepositoryImpl implements NotificationRuleRepository {

    private final NotificationRulePersistenceRepository jpaRepository;

    public NotificationRuleRepositoryImpl(NotificationRulePersistenceRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public NotificationRule save(NotificationRule rule) {
        return NotificationsPersistenceAssembler.toDomain(
                jpaRepository.save(NotificationsPersistenceAssembler.toEntity(rule)));
    }

    @Override
    public Optional<NotificationRule> findByIdAndTenantId(UUID id, UUID tenantId) {
        return jpaRepository.findByIdAndTenantId(id, tenantId).map(NotificationsPersistenceAssembler::toDomain);
    }

    @Override
    public List<NotificationRule> findByTenantId(UUID tenantId) {
        return jpaRepository.findByTenantIdOrderByUserIdAscAssetTypeAscChannelAsc(tenantId).stream()
                .map(NotificationsPersistenceAssembler::toDomain)
                .toList();
    }

    @Override
    public List<NotificationRule> findMatching(UUID tenantId, String assetType) {
        List<String> types = assetType == null || assetType.isBlank()
                ? List.of(NotificationRule.ANY_TYPE)
                : List.of(NotificationRule.ANY_TYPE, NotificationRule.normalize(assetType));
        return jpaRepository.findByTenantIdAndAssetTypeIn(tenantId, types).stream()
                .map(NotificationsPersistenceAssembler::toDomain)
                .toList();
    }

    @Override
    public boolean exists(UUID tenantId, UUID userId, String assetType, NotificationChannel channel) {
        return jpaRepository.existsByTenantIdAndUserIdAndAssetTypeAndChannel(
                tenantId, userId, NotificationRule.normalize(assetType), channel);
    }

    @Override
    public void delete(UUID id) {
        jpaRepository.deleteById(id);
    }
}