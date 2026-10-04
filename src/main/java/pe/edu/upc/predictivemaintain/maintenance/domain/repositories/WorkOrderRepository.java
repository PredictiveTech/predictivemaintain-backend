package pe.edu.upc.predictivemaintain.maintenance.domain.repositories;

import pe.edu.upc.predictivemaintain.maintenance.domain.model.aggregates.WorkOrder;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.aggregates.WorkOrderChange;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.valueobjects.WorkOrderStatus;
import pe.edu.upc.predictivemaintain.shared.domain.model.valueobjects.PageQuery;
import pe.edu.upc.predictivemaintain.shared.domain.model.valueobjects.PagedResult;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkOrderRepository {

    /** Saves the order and the history entries it has accumulated. */
    WorkOrder save(WorkOrder order);

    Optional<WorkOrder> findByIdAndTenantId(UUID id, UUID tenantId);

    Optional<WorkOrder> findByAlertIdAndTenantId(UUID alertId, UUID tenantId);

    /** assignedUserId and status are optional filters. Newest first. */
    PagedResult<WorkOrder> search(UUID tenantId, UUID assignedUserId, WorkOrderStatus status, PageQuery page);

    /** Orders still open (OPEN, ASSIGNED or IN_PROGRESS) for the alerts of one asset. */
    long countOpenByAssetId(UUID tenantId, UUID assetId);

    /** History of one order, oldest first. */
    List<WorkOrderChange> findHistory(UUID tenantId, UUID workOrderId);
}