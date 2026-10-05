package pe.edu.upc.predictivemaintain.notifications.infrastructure.persistence.jpa.adapters;

import org.springframework.stereotype.Repository;
import pe.edu.upc.predictivemaintain.notifications.domain.model.aggregates.DeviceToken;
import pe.edu.upc.predictivemaintain.notifications.domain.repositories.DeviceTokenRepository;
import pe.edu.upc.predictivemaintain.notifications.infrastructure.persistence.jpa.assemblers.NotificationsPersistenceAssembler;
import pe.edu.upc.predictivemaintain.notifications.infrastructure.persistence.jpa.entities.DeviceTokenPersistenceEntity;
import pe.edu.upc.predictivemaintain.notifications.infrastructure.persistence.jpa.repositories.DeviceTokenPersistenceRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class DeviceTokenRepositoryImpl implements DeviceTokenRepository {

    private final DeviceTokenPersistenceRepository jpaRepository;

    public DeviceTokenRepositoryImpl(DeviceTokenPersistenceRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public DeviceToken save(DeviceToken deviceToken) {
        DeviceTokenPersistenceEntity entity = jpaRepository.findById(deviceToken.getId())
                .orElseGet(DeviceTokenPersistenceEntity::new);
        NotificationsPersistenceAssembler.copyToEntity(deviceToken, entity);
        return NotificationsPersistenceAssembler.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<DeviceToken> findByToken(String token) {
        return jpaRepository.findByToken(token).map(NotificationsPersistenceAssembler::toDomain);
    }

    @Override
    public Optional<DeviceToken> findByIdAndUserId(UUID id, UUID userId) {
        return jpaRepository.findByIdAndUserId(id, userId).map(NotificationsPersistenceAssembler::toDomain);
    }

    @Override
    public List<DeviceToken> findByTenantIdAndUserId(UUID tenantId, UUID userId) {
        return jpaRepository.findByTenantIdAndUserId(tenantId, userId).stream()
                .map(NotificationsPersistenceAssembler::toDomain)
                .toList();
    }

    @Override
    public void delete(UUID id) {
        jpaRepository.deleteById(id);
    }
}