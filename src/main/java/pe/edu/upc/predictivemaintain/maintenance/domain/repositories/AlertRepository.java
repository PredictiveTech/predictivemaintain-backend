package pe.edu.upc.predictivemaintain.maintenance.domain.repositories;

import pe.edu.upc.predictivemaintain.maintenance.domain.model.aggregates.Alert;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.valueobjects.AlertSeverity;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.valueobjects.AlertStatus;
import pe.edu.upc.predictivemaintain.shared.domain.model.valueobjects.PageQuery;
import pe.edu.upc.predictivemaintain.shared.domain.model.valueobjects.PagedResult;

import java.util.Optional;
import java.util.UUID;

public interface AlertRepository {

    Alert save(Alert alert);

    Optional<Alert> findByIdAndTenantId(UUID id, UUID tenantId);

    Optional<Alert> findByTenantIdAndSourceEventId(UUID tenantId, UUID sourceEventId);

    PagedResult<Alert> search(UUID tenantId, AlertSeverity severity, AlertStatus status,
                              UUID assetId, PageQuery page);
}