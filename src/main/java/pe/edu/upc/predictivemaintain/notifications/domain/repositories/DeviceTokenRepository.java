package pe.edu.upc.predictivemaintain.notifications.domain.repositories;

import pe.edu.upc.predictivemaintain.notifications.domain.model.aggregates.DeviceToken;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeviceTokenRepository {

    DeviceToken save(DeviceToken deviceToken);

    /** Without company: a token is unique in the whole platform, whoever it belonged to. */
    Optional<DeviceToken> findByToken(String token);

    Optional<DeviceToken> findByIdAndUserId(UUID id, UUID userId);

    List<DeviceToken> findByTenantIdAndUserId(UUID tenantId, UUID userId);

    void delete(UUID id);
}