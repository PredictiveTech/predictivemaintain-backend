package pe.edu.upc.predictivemaintain.maintenance.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.predictivemaintain.maintenance.infrastructure.persistence.jpa.entities.WorkOrderChangePersistenceEntity;

import java.util.List;
import java.util.UUID;

public interface WorkOrderChangePersistenceRepository
        extends JpaRepository<WorkOrderChangePersistenceEntity, UUID> {

    List<WorkOrderChangePersistenceEntity> findByTenantIdAndWorkOrderIdOrderByChangedAtAsc(
            UUID tenantId, UUID workOrderId);
}