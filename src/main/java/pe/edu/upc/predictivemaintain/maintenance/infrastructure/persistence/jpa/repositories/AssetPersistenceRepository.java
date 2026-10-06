package pe.edu.upc.predictivemaintain.maintenance.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import pe.edu.upc.predictivemaintain.maintenance.infrastructure.persistence.jpa.entities.AssetPersistenceEntity;

import java.util.Optional;
import java.util.UUID;
import java.util.Collection;
import java.util.List;

/**
 * JpaSpecificationExecutor lets the adapter build the search filters dynamically.
 */
public interface AssetPersistenceRepository
        extends JpaRepository<AssetPersistenceEntity, UUID>, JpaSpecificationExecutor<AssetPersistenceEntity> {

    Optional<AssetPersistenceEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    boolean existsByTenantIdAndCode(UUID tenantId, String code);

    List<AssetPersistenceEntity> findByTenantIdAndIdIn(UUID tenantId, Collection<UUID> ids);
}