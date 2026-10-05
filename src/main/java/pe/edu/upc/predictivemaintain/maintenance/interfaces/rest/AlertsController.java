package pe.edu.upc.predictivemaintain.maintenance.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upc.predictivemaintain.iam.interfaces.acl.AuthenticatedUser;
import pe.edu.upc.predictivemaintain.maintenance.application.commandservices.AlertCommandService;
import pe.edu.upc.predictivemaintain.maintenance.application.queryservices.AlertQueryService;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.aggregates.Alert;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.commands.ConfirmAlertCommand;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.commands.DiscardAlertCommand;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.queries.GetAlertByIdQuery;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.queries.GetAllAlertsQuery;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.valueobjects.AlertSeverity;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.valueobjects.AlertStatus;
import pe.edu.upc.predictivemaintain.maintenance.interfaces.rest.resources.AlertResource;
import pe.edu.upc.predictivemaintain.maintenance.interfaces.rest.resources.UpdateAlertStatusResource;
import pe.edu.upc.predictivemaintain.maintenance.interfaces.rest.transform.AlertResourceFromEntityAssembler;
import pe.edu.upc.predictivemaintain.shared.domain.exceptions.DomainValidationException;
import pe.edu.upc.predictivemaintain.shared.domain.model.valueobjects.PageQuery;
import pe.edu.upc.predictivemaintain.shared.interfaces.rest.resources.PagedResource;

import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/alerts", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Alerts", description = "Predictive alerts raised for the company's assets")
@SecurityRequirement(name = "bearerAuth")
public class AlertsController {

    private final AlertCommandService alertCommandService;
    private final AlertQueryService alertQueryService;

    public AlertsController(AlertCommandService alertCommandService, AlertQueryService alertQueryService) {
        this.alertCommandService = alertCommandService;
        this.alertQueryService = alertQueryService;
    }

    @GetMapping
    @Operation(summary = "List alerts",
            description = "Paginated, newest first (US-04). Filter by severity, status or asset. Open to every role.")
    public ResponseEntity<PagedResource<AlertResource>> list(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @RequestParam(required = false) AlertSeverity severity,
            @RequestParam(required = false) AlertStatus status,
            @RequestParam(required = false) UUID assetId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var result = alertQueryService.handle(new GetAllAlertsQuery(
                principal.tenantId(), severity, status, assetId, new PageQuery(page, size)));
        return ResponseEntity.ok(PagedResource.from(result, AlertResourceFromEntityAssembler::toResource));
    }

    @GetMapping("/{alertId}")
    @Operation(summary = "Get an alert",
            description = "Detail of one alert (US-05). Open to every role. The diagnostic variables "
                    + "(values and exceeded threshold) are added with the Telemetry context.")
    public ResponseEntity<AlertResource> getById(@AuthenticationPrincipal AuthenticatedUser principal,
                                                 @PathVariable UUID alertId) {
        Alert alert = alertQueryService.handle(new GetAlertByIdQuery(principal.tenantId(), alertId));
        return ResponseEntity.ok(AlertResourceFromEntityAssembler.toResource(alert));
    }

    @PatchMapping("/{alertId}/status")
    @PreAuthorize("hasRole('MAINTENANCE_MANAGER')")
    @Operation(summary = "Confirm or discard an alert",
            description = "Only for maintenance managers (US-04). Discarding requires a reason. "
                    + "Only alerts IN_REVIEW can change; otherwise the answer is 409.")
    public ResponseEntity<AlertResource> updateStatus(@AuthenticationPrincipal AuthenticatedUser principal,
                                                      @PathVariable UUID alertId,
                                                      @Valid @RequestBody UpdateAlertStatusResource resource) {
        Alert alert = switch (resource.status()) {
            case CONFIRMED -> alertCommandService.handle(
                    new ConfirmAlertCommand(principal.tenantId(), alertId, resource.expectedVersion()));
            case DISCARDED -> alertCommandService.handle(
                    new DiscardAlertCommand(principal.tenantId(), alertId, resource.reason(),
                            resource.expectedVersion()));
            default -> throw new DomainValidationException("validation.alert.status-not-allowed");
        };
        return ResponseEntity.ok(AlertResourceFromEntityAssembler.toResource(alert));
    }
}