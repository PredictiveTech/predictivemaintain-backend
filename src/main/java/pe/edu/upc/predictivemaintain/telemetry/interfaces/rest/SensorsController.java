package pe.edu.upc.predictivemaintain.telemetry.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upc.predictivemaintain.iam.interfaces.acl.AuthenticatedUser;
import pe.edu.upc.predictivemaintain.telemetry.application.commandservices.SensorCommandService;
import pe.edu.upc.predictivemaintain.telemetry.domain.model.aggregates.ThresholdRule;
import pe.edu.upc.predictivemaintain.telemetry.domain.model.commands.ConfigureThresholdCommand;
import pe.edu.upc.predictivemaintain.telemetry.interfaces.rest.resources.ConfigureThresholdResource;
import pe.edu.upc.predictivemaintain.telemetry.interfaces.rest.resources.ThresholdResource;
import pe.edu.upc.predictivemaintain.telemetry.interfaces.rest.transform.SensorResourceFromEntityAssembler;

import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/sensors", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Telemetry", description = "Sensors, readings and thresholds of the company's assets")
@SecurityRequirement(name = "bearerAuth")
public class SensorsController {

    private final SensorCommandService sensorCommandService;

    public SensorsController(SensorCommandService sensorCommandService) {
        this.sensorCommandService = sensorCommandService;
    }

    @PutMapping("/{sensorId}/threshold")
    @PreAuthorize("hasRole('MAINTENANCE_MANAGER')")
    @Operation(summary = "Configure the threshold of a sensor",
            description = "Only for maintenance managers (US-03). Creates the rule or changes it, increasing "
                    + "its version. The lower bound must be smaller than the upper bound. It applies to future readings.")
    public ResponseEntity<ThresholdResource> configureThreshold(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @PathVariable UUID sensorId,
            @Valid @RequestBody ConfigureThresholdResource resource) {
        ThresholdRule rule = sensorCommandService.handle(new ConfigureThresholdCommand(
                principal.tenantId(), sensorId, resource.lowerBound(), resource.upperBound(), resource.severity()));
        return ResponseEntity.ok(SensorResourceFromEntityAssembler.toResource(rule));
    }
}