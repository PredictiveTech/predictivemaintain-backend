package pe.edu.upc.predictivemaintain.maintenance.application.internal.queryservices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.predictivemaintain.maintenance.application.errors.MaintenanceError;
import pe.edu.upc.predictivemaintain.maintenance.application.outboundservices.EvidenceStorage;
import pe.edu.upc.predictivemaintain.maintenance.application.queryservices.EvidenceContent;
import pe.edu.upc.predictivemaintain.maintenance.application.queryservices.WorkOrderQueryService;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.aggregates.EvidencePhoto;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.aggregates.WorkOrder;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.aggregates.WorkOrderChange;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.queries.GetAllWorkOrdersQuery;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.queries.GetEvidenceContentQuery;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.queries.GetWorkOrderByIdQuery;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.queries.GetWorkOrderEvidenceQuery;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.queries.GetWorkOrderHistoryQuery;
import pe.edu.upc.predictivemaintain.maintenance.domain.repositories.EvidencePhotoRepository;
import pe.edu.upc.predictivemaintain.maintenance.domain.repositories.WorkOrderRepository;
import pe.edu.upc.predictivemaintain.shared.application.errors.ApplicationException;
import pe.edu.upc.predictivemaintain.shared.domain.model.valueobjects.PagedResult;

import java.util.List;
import java.util.UUID;

@Service
public class WorkOrderQueryServiceImpl implements WorkOrderQueryService {

    private final WorkOrderRepository workOrderRepository;
    private final EvidencePhotoRepository evidenceRepository;
    private final EvidenceStorage evidenceStorage;

    public WorkOrderQueryServiceImpl(WorkOrderRepository workOrderRepository,
                                     EvidencePhotoRepository evidenceRepository,
                                     EvidenceStorage evidenceStorage) {
        this.workOrderRepository = workOrderRepository;
        this.evidenceRepository = evidenceRepository;
        this.evidenceStorage = evidenceStorage;
    }

    @Override
    @Transactional(readOnly = true)
    public WorkOrder handle(GetWorkOrderByIdQuery query) {
        return loadVisible(query.tenantId(), query.restrictToUserId(), query.workOrderId());
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResult<WorkOrder> handle(GetAllWorkOrdersQuery query) {
        return workOrderRepository.search(query.tenantId(), query.restrictToUserId(), query.status(), query.page());
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkOrderChange> handle(GetWorkOrderHistoryQuery query) {
        loadVisible(query.tenantId(), query.restrictToUserId(), query.workOrderId());
        return workOrderRepository.findHistory(query.tenantId(), query.workOrderId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EvidencePhoto> handle(GetWorkOrderEvidenceQuery query) {
        loadVisible(query.tenantId(), query.restrictToUserId(), query.workOrderId());
        return evidenceRepository.findByWorkOrderId(query.tenantId(), query.workOrderId());
    }

    /** Not transactional on purpose: reading the file from disk should not hold a database connection. */
    @Override
    public EvidenceContent handle(GetEvidenceContentQuery query) {
        loadVisible(query.tenantId(), query.restrictToUserId(), query.workOrderId());
        EvidencePhoto photo = evidenceRepository
                .findByIdAndWorkOrderId(query.tenantId(), query.workOrderId(), query.evidenceId())
                .orElseThrow(() -> new ApplicationException(MaintenanceError.ATTACHMENT_NOT_FOUND));
        return new EvidenceContent(photo.mimeType(), evidenceStorage.load(photo.storageKey()));
    }

    /**
     * A technician asking for an order that is not his gets "not found", the same answer as an order
     * that does not exist: it does not reveal what other technicians are working on.
     */
    private WorkOrder loadVisible(UUID tenantId, UUID restrictToUserId, UUID workOrderId) {
        WorkOrder order = workOrderRepository.findByIdAndTenantId(workOrderId, tenantId)
                .orElseThrow(() -> new ApplicationException(MaintenanceError.WORK_ORDER_NOT_FOUND));
        if (restrictToUserId != null && !order.isAssignedTo(restrictToUserId)) {
            throw new ApplicationException(MaintenanceError.WORK_ORDER_NOT_FOUND);
        }
        return order;
    }
}