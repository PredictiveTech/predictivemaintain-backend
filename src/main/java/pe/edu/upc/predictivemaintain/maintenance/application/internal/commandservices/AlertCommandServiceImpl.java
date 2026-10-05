package pe.edu.upc.predictivemaintain.maintenance.application.internal.commandservices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.predictivemaintain.maintenance.application.commandservices.AlertCommandService;
import pe.edu.upc.predictivemaintain.maintenance.application.errors.MaintenanceError;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.aggregates.Alert;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.aggregates.Asset;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.commands.ConfirmAlertCommand;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.commands.DiscardAlertCommand;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.commands.RegisterAlertCommand;
import pe.edu.upc.predictivemaintain.maintenance.domain.repositories.AlertRepository;
import pe.edu.upc.predictivemaintain.maintenance.domain.repositories.AssetRepository;
import pe.edu.upc.predictivemaintain.shared.application.errors.ApplicationException;

import java.time.Clock;
import java.util.Optional;
import java.util.UUID;

@Service
public class AlertCommandServiceImpl implements AlertCommandService {

    private final AlertRepository alertRepository;
    private final AssetRepository assetRepository;
    private final Clock clock;

    public AlertCommandServiceImpl(AlertRepository alertRepository, AssetRepository assetRepository, Clock clock) {
        this.alertRepository = alertRepository;
        this.assetRepository = assetRepository;
        this.clock = clock;
    }

    @Override
    @Transactional
    public Alert handle(RegisterAlertCommand command) {
        // Idempotent: the same anomaly event never creates two alerts.
        Optional<Alert> existing =
                alertRepository.findByTenantIdAndSourceEventId(command.tenantId(), command.sourceEventId());
        if (existing.isPresent()) {
            return existing.get();
        }
        Asset asset = assetRepository.findByIdAndTenantId(command.assetId(), command.tenantId())
                .orElseThrow(() -> new ApplicationException(MaintenanceError.ASSET_NOT_FOUND));
        if (!asset.isActive()) {
            throw new ApplicationException(MaintenanceError.ASSET_INACTIVE);
        }
        return alertRepository.save(Alert.raise(command.tenantId(), command.assetId(), command.sourceEventId(),
                command.severity(), command.diagnostic(), clock.instant()));
    }

    @Override
    @Transactional
    public Alert handle(ConfirmAlertCommand command) {
        Alert alert = findAlert(command.tenantId(), command.alertId());
        alert.assertExpectedVersion(command.expectedVersion());
        alert.confirm();
        return alertRepository.save(alert);
    }

    @Override
    @Transactional
    public Alert handle(DiscardAlertCommand command) {
        Alert alert = findAlert(command.tenantId(), command.alertId());
        alert.assertExpectedVersion(command.expectedVersion());
        alert.discard(command.reason());
        return alertRepository.save(alert);
    }

    private Alert findAlert(UUID tenantId, UUID alertId) {
        return alertRepository.findByIdAndTenantId(alertId, tenantId)
                .orElseThrow(() -> new ApplicationException(MaintenanceError.ALERT_NOT_FOUND));
    }
}