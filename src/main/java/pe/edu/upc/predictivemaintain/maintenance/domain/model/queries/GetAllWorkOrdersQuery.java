package pe.edu.upc.predictivemaintain.maintenance.domain.model.queries;

import pe.edu.upc.predictivemaintain.maintenance.domain.model.valueobjects.WorkOrderStatus;
import pe.edu.upc.predictivemaintain.shared.domain.model.valueobjects.PageQuery;

import java.util.UUID;

/**
 * @param restrictToUserId when not null, only the orders assigned to that user are returned
 */
public record GetAllWorkOrdersQuery(UUID tenantId, UUID restrictToUserId, WorkOrderStatus status, PageQuery page) {
}