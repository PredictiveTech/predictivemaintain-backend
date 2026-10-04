package pe.edu.upc.predictivemaintain.maintenance.application.queryservices;

import pe.edu.upc.predictivemaintain.maintenance.domain.model.aggregates.EvidencePhoto;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.aggregates.WorkOrder;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.aggregates.WorkOrderChange;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.queries.GetAllWorkOrdersQuery;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.queries.GetEvidenceContentQuery;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.queries.GetWorkOrderByIdQuery;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.queries.GetWorkOrderEvidenceQuery;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.queries.GetWorkOrderHistoryQuery;
import pe.edu.upc.predictivemaintain.shared.domain.model.valueobjects.PagedResult;

import java.util.List;

public interface WorkOrderQueryService {

    WorkOrder handle(GetWorkOrderByIdQuery query);

    PagedResult<WorkOrder> handle(GetAllWorkOrdersQuery query);

    List<WorkOrderChange> handle(GetWorkOrderHistoryQuery query);

    List<EvidencePhoto> handle(GetWorkOrderEvidenceQuery query);

    EvidenceContent handle(GetEvidenceContentQuery query);
}