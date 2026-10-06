package pe.edu.upc.predictivemaintain.maintenance.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import pe.edu.upc.predictivemaintain.maintenance.infrastructure.persistence.jpa.entities.AlertPersistenceEntity;

import java.util.Optional;
import java.util.UUID;
import java.util.Collection;
import java.util.List;

public interface AlertPersistenceRepository
        extends JpaRepository<AlertPersistenceEntity, UUID>, JpaSpecificationExecutor<AlertPersistenceEntity> {

    Optional<AlertPersistenceEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    Optional<AlertPersistenceEntity> findByTenantIdAndSourceEventId(UUID tenantId, UUID sourceEventId);

    List<AlertPersistenceEntity> findByTenantIdAndIdIn(UUID tenantId, Collection<UUID> ids);
}