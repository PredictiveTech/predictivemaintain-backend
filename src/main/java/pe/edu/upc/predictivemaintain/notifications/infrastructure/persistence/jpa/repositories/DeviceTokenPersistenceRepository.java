package pe.edu.upc.predictivemaintain.notifications.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.predictivemaintain.notifications.infrastructure.persistence.jpa.entities.DeviceTokenPersistenceEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeviceTokenPersistenceRepository extends JpaRepository<DeviceTokenPersistenceEntity, UUID> {

    Optional<DeviceTokenPersistenceEntity> findByToken(String token);

    Optional<DeviceTokenPersistenceEntity> findByIdAndUserId(UUID id, UUID userId);

    List<DeviceTokenPersistenceEntity> findByTenantIdAndUserId(UUID tenantId, UUID userId);
}