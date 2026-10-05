package pe.edu.upc.predictivemaintain.maintenance.domain.repositories;

import pe.edu.upc.predictivemaintain.maintenance.domain.model.aggregates.SyncOperationRecord;

import java.util.Optional;
import java.util.UUID;

public interface SyncOperationRepository {

    SyncOperationRecord save(SyncOperationRecord record);

    Optional<SyncOperationRecord> findByTenantIdAndOperationId(UUID tenantId, UUID operationId);
}