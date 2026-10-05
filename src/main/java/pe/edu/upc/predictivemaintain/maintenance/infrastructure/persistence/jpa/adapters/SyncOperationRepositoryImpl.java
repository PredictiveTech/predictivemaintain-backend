package pe.edu.upc.predictivemaintain.maintenance.infrastructure.persistence.jpa.adapters;

import org.springframework.stereotype.Repository;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.aggregates.SyncOperationRecord;
import pe.edu.upc.predictivemaintain.maintenance.domain.repositories.SyncOperationRepository;
import pe.edu.upc.predictivemaintain.maintenance.infrastructure.persistence.jpa.assemblers.SyncOperationPersistenceAssembler;
import pe.edu.upc.predictivemaintain.maintenance.infrastructure.persistence.jpa.repositories.SyncOperationPersistenceRepository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class SyncOperationRepositoryImpl implements SyncOperationRepository {

    private final SyncOperationPersistenceRepository jpaRepository;

    public SyncOperationRepositoryImpl(SyncOperationPersistenceRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public SyncOperationRecord save(SyncOperationRecord record) {
        return SyncOperationPersistenceAssembler.toDomain(
                jpaRepository.save(SyncOperationPersistenceAssembler.toEntity(record)));
    }

    @Override
    public Optional<SyncOperationRecord> findByTenantIdAndOperationId(UUID tenantId, UUID operationId) {
        return jpaRepository.findByTenantIdAndOperationId(tenantId, operationId)
                .map(SyncOperationPersistenceAssembler::toDomain);
    }
}