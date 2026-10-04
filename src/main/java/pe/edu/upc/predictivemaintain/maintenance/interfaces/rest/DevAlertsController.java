package pe.edu.upc.predictivemaintain.maintenance.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upc.predictivemaintain.iam.interfaces.acl.AuthenticatedUser;
import pe.edu.upc.predictivemaintain.maintenance.application.commandservices.AlertCommandService;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.aggregates.Alert;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.commands.RegisterAlertCommand;
import pe.edu.upc.predictivemaintain.maintenance.interfaces.rest.resources.AlertResource;
import pe.edu.upc.predictivemaintain.maintenance.interfaces.rest.resources.SimulateAlertResource;
import pe.edu.upc.predictivemaintain.maintenance.interfaces.rest.transform.AlertResourceFromEntityAssembler;

import java.util.UUID;

/**
 * TEMPORARY. Lets you test alerts and work orders before Telemetry exists. It only exists in the
 * "dev" profile and is deleted in part D1, when Telemetry starts creating the real alerts.
 */
@Profile("dev")
@RestController
@RequestMapping(value = "/api/v1/dev/alerts", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Development tools", description = "Temporary endpoints available only in the dev profile")
@SecurityRequirement(name = "bearerAuth")
public class DevAlertsController {

    private final AlertCommandService alertCommandService;

    public DevAlertsController(AlertCommandService alertCommandService) {
        this.alertCommandService = alertCommandService;
    }

    @PostMapping
    @PreAuthorize("hasRole('MAINTENANCE_MANAGER')")
    @Operation(summary = "Simulate an alert", description = "Creates an IN_REVIEW alert for one of your assets.")
    public ResponseEntity<AlertResource> simulate(@AuthenticationPrincipal AuthenticatedUser principal,
                                                  @Valid @RequestBody SimulateAlertResource resource) {
        Alert alert = alertCommandService.handle(new RegisterAlertCommand(
                principal.tenantId(), resource.assetId(), UUID.randomUUID(), resource.severity()));
        return ResponseEntity.status(HttpStatus.CREATED).body(AlertResourceFromEntityAssembler.toResource(alert));
    }
}