package pe.edu.upc.predictivemaintain.maintenance.application.internal.queryservices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.predictivemaintain.maintenance.application.errors.MaintenanceError;
import pe.edu.upc.predictivemaintain.maintenance.application.queryservices.AlertQueryService;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.aggregates.Alert;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.queries.GetAlertByIdQuery;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.queries.GetAllAlertsQuery;
import pe.edu.upc.predictivemaintain.maintenance.domain.repositories.AlertRepository;
import pe.edu.upc.predictivemaintain.shared.application.errors.ApplicationException;
import pe.edu.upc.predictivemaintain.shared.domain.model.valueobjects.PagedResult;

@Service
public class AlertQueryServiceImpl implements AlertQueryService {

    private final AlertRepository alertRepository;

    public AlertQueryServiceImpl(AlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Alert handle(GetAlertByIdQuery query) {
        return alertRepository.findByIdAndTenantId(query.alertId(), query.tenantId())
                .orElseThrow(() -> new ApplicationException(MaintenanceError.ALERT_NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResult<Alert> handle(GetAllAlertsQuery query) {
        return alertRepository.search(query.tenantId(), query.severity(), query.status(),
                query.assetId(), query.page());
    }
}