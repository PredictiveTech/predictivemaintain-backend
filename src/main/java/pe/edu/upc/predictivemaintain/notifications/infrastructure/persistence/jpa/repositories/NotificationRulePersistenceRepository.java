package pe.edu.upc.predictivemaintain.notifications.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.predictivemaintain.notifications.domain.model.valueobjects.NotificationChannel;
import pe.edu.upc.predictivemaintain.notifications.infrastructure.persistence.jpa.entities.NotificationRulePersistenceEntity;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRulePersistenceRepository extends JpaRepository<NotificationRulePersistenceEntity, UUID> {

    Optional<NotificationRulePersistenceEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    List<NotificationRulePersistenceEntity> findByTenantIdOrderByUserIdAscAssetTypeAscChannelAsc(UUID tenantId);

    List<NotificationRulePersistenceEntity> findByTenantIdAndAssetTypeIn(UUID tenantId, Collection<String> assetTypes);

    boolean existsByTenantIdAndUserIdAndAssetTypeAndChannel(UUID tenantId, UUID userId, String assetType,
                                                            NotificationChannel channel);
}