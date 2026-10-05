package pe.edu.upc.predictivemaintain.maintenance.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.predictivemaintain.maintenance.infrastructure.persistence.jpa.entities.EvidencePhotoPersistenceEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EvidencePhotoPersistenceRepository extends JpaRepository<EvidencePhotoPersistenceEntity, UUID> {

    List<EvidencePhotoPersistenceEntity> findByTenantIdAndWorkOrderIdOrderByUploadedAtAsc(
            UUID tenantId, UUID workOrderId);

    Optional<EvidencePhotoPersistenceEntity> findByIdAndTenantIdAndWorkOrderId(
            UUID id, UUID tenantId, UUID workOrderId);
}