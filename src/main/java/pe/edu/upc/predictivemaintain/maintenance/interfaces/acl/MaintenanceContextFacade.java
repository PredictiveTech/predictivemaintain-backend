package pe.edu.upc.predictivemaintain.maintenance.interfaces.acl;

import org.springframework.stereotype.Service;
import pe.edu.upc.predictivemaintain.maintenance.application.commandservices.AlertCommandService;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.aggregates.Alert;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.commands.RegisterAlertCommand;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.valueobjects.AlertDiagnostic;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.valueobjects.AlertSeverity;
import pe.edu.upc.predictivemaintain.maintenance.domain.repositories.AssetRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Anti-Corruption Layer: what the Maintenance context exposes to other bounded contexts.
 */
@Service
public class MaintenanceContextFacade {

    /** NOT_FOUND also covers assets of another company. */
    public enum AssetState {
        ACTIVE,
        INACTIVE,
        NOT_FOUND
    }

    /** The data a reading provides to explain why an alert was raised. Simple types only. */
    public record AlertEvidence(String metric, String unit, BigDecimal observedValue,
                                BigDecimal lowerBound, BigDecimal upperBound, Instant measuredAt) {
    }

    /** What a message about an asset needs to say. */
    public record AssetSummary(String code, String name, String assetType) {
    }

    private final AssetRepository assetRepository;
    private final AlertCommandService alertCommandService;

    public MaintenanceContextFacade(AssetRepository assetRepository, AlertCommandService alertCommandService) {
        this.assetRepository = assetRepository;
        this.alertCommandService = alertCommandService;
    }

    public AssetState assetState(UUID tenantId, UUID assetId) {
        return assetRepository.findByIdAndTenantId(assetId, tenantId)
                .map(asset -> asset.isActive() ? AssetState.ACTIVE : AssetState.INACTIVE)
                .orElse(AssetState.NOT_FOUND);
    }

    public Optional<AssetSummary> assetSummary(UUID tenantId, UUID assetId) {
        return assetRepository.findByIdAndTenantId(assetId, tenantId)
                .map(asset -> new AssetSummary(asset.getCode(), asset.getName(), asset.getAssetType()));
    }

    /**
     * Raises an alert for an anomaly and returns its id. Idempotent by sourceEventId: reporting the same
     * event twice returns the same alert.
     *
     * @param severity WARNING or CRITICAL
     */
    public UUID registerAlert(UUID tenantId, UUID assetId, UUID sourceEventId, String severity,
                              AlertEvidence evidence) {
        AlertDiagnostic diagnostic = new AlertDiagnostic(evidence.metric(), evidence.unit(),
                evidence.observedValue(), evidence.lowerBound(), evidence.upperBound(), evidence.measuredAt());
        Alert alert = alertCommandService.handle(new RegisterAlertCommand(
                tenantId, assetId, sourceEventId, AlertSeverity.valueOf(severity), diagnostic));
        return alert.getId();
    }
}