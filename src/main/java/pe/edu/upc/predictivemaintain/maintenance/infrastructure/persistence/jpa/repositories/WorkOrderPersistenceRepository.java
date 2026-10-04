package pe.edu.upc.predictivemaintain.maintenance.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.valueobjects.WorkOrderStatus;
import pe.edu.upc.predictivemaintain.maintenance.infrastructure.persistence.jpa.entities.WorkOrderPersistenceEntity;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface WorkOrderPersistenceRepository
        extends JpaRepository<WorkOrderPersistenceEntity, UUID>, JpaSpecificationExecutor<WorkOrderPersistenceEntity> {

    Optional<WorkOrderPersistenceEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    Optional<WorkOrderPersistenceEntity> findByAlertIdAndTenantId(UUID alertId, UUID tenantId);

    /** An order belongs to an asset through its alert, so the two tables are joined by alert id. */
    @Query("select count(w) from WorkOrderPersistenceEntity w, AlertPersistenceEntity a "
            + "where w.alertId = a.id and w.tenantId = :tenantId and a.assetId = :assetId "
            + "and w.status in :statuses")
    long countByAssetIdAndStatusIn(@Param("tenantId") UUID tenantId,
                                   @Param("assetId") UUID assetId,
                                   @Param("statuses") Collection<WorkOrderStatus> statuses);
}