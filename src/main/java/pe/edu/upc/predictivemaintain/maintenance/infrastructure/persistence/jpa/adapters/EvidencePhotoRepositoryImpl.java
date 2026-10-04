package pe.edu.upc.predictivemaintain.maintenance.infrastructure.persistence.jpa.adapters;

import org.springframework.stereotype.Repository;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.aggregates.EvidencePhoto;
import pe.edu.upc.predictivemaintain.maintenance.domain.repositories.EvidencePhotoRepository;
import pe.edu.upc.predictivemaintain.maintenance.infrastructure.persistence.jpa.assemblers.EvidencePhotoPersistenceAssembler;
import pe.edu.upc.predictivemaintain.maintenance.infrastructure.persistence.jpa.repositories.EvidencePhotoPersistenceRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class EvidencePhotoRepositoryImpl implements EvidencePhotoRepository {

    private final EvidencePhotoPersistenceRepository jpaRepository;

    public EvidencePhotoRepositoryImpl(EvidencePhotoPersistenceRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public EvidencePhoto save(EvidencePhoto photo) {
        return EvidencePhotoPersistenceAssembler.toDomain(
                jpaRepository.save(EvidencePhotoPersistenceAssembler.toEntity(photo)));
    }

    @Override
    public List<EvidencePhoto> findByWorkOrderId(UUID tenantId, UUID workOrderId) {
        return jpaRepository.findByTenantIdAndWorkOrderIdOrderByUploadedAtAsc(tenantId, workOrderId).stream()
                .map(EvidencePhotoPersistenceAssembler::toDomain)
                .toList();
    }

    @Override
    public Optional<EvidencePhoto> findByIdAndWorkOrderId(UUID tenantId, UUID workOrderId, UUID evidenceId) {
        return jpaRepository.findByIdAndTenantIdAndWorkOrderId(evidenceId, tenantId, workOrderId)
                .map(EvidencePhotoPersistenceAssembler::toDomain);
    }
}