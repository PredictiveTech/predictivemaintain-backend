package pe.edu.upc.predictivemaintain.maintenance.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.predictivemaintain.maintenance.infrastructure.persistence.jpa.entities.SyncOperationPersistenceEntity;

import java.util.Optional;
import java.util.UUID;

public interface SyncOperationPersistenceRepository extends JpaRepository<SyncOperationPersistenceEntity, UUID> {

    Optional<SyncOperationPersistenceEntity> findByTenantIdAndOperationId(UUID tenantId, UUID operationId);
}