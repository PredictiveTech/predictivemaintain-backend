package pe.edu.upc.predictivemaintain.maintenance.application.queryservices;

import pe.edu.upc.predictivemaintain.maintenance.domain.model.aggregates.Alert;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.queries.GetAlertByIdQuery;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.queries.GetAllAlertsQuery;
import pe.edu.upc.predictivemaintain.shared.domain.model.valueobjects.PagedResult;

public interface AlertQueryService {

    Alert handle(GetAlertByIdQuery query);

    PagedResult<Alert> handle(GetAllAlertsQuery query);
}