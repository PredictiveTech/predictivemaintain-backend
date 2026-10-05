package pe.edu.upc.predictivemaintain.maintenance.domain.repositories;

import pe.edu.upc.predictivemaintain.maintenance.domain.model.aggregates.EvidencePhoto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EvidencePhotoRepository {

    EvidencePhoto save(EvidencePhoto photo);

    List<EvidencePhoto> findByWorkOrderId(UUID tenantId, UUID workOrderId);

    Optional<EvidencePhoto> findByIdAndWorkOrderId(UUID tenantId, UUID workOrderId, UUID evidenceId);
}