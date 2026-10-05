package pe.edu.upc.predictivemaintain.telemetry.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upc.predictivemaintain.iam.interfaces.acl.AuthenticatedUser;
import pe.edu.upc.predictivemaintain.telemetry.application.commandservices.IngestReadingResult;
import pe.edu.upc.predictivemaintain.telemetry.application.commandservices.ReadingCommandService;
import pe.edu.upc.predictivemaintain.telemetry.application.commandservices.SensorCommandService;
import pe.edu.upc.predictivemaintain.telemetry.domain.model.aggregates.ThresholdRule;
import pe.edu.upc.predictivemaintain.telemetry.domain.model.commands.ConfigureThresholdCommand;
import pe.edu.upc.predictivemaintain.telemetry.domain.model.commands.IngestReadingCommand;
import pe.edu.upc.predictivemaintain.telemetry.interfaces.rest.resources.ConfigureThresholdResource;
import pe.edu.upc.predictivemaintain.telemetry.interfaces.rest.resources.IngestReadingResource;
import pe.edu.upc.predictivemaintain.telemetry.interfaces.rest.resources.ReadingAcceptedResource;
import pe.edu.upc.predictivemaintain.telemetry.interfaces.rest.resources.ThresholdResource;
import pe.edu.upc.predictivemaintain.telemetry.interfaces.rest.transform.ReadingAcceptedResourceAssembler;
import pe.edu.upc.predictivemaintain.telemetry.interfaces.rest.transform.SensorResourceFromEntityAssembler;

import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/sensors", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Telemetry", description = "Sensors, readings and thresholds of the company's assets")
public class SensorsController {

    private final SensorCommandService sensorCommandService;
    private final ReadingCommandService readingCommandService;

    public SensorsController(SensorCommandService sensorCommandService,
                             ReadingCommandService readingCommandService) {
        this.sensorCommandService = sensorCommandService;
        this.readingCommandService = readingCommandService;
    }

    @PostMapping("/readings")
    @Operation(summary = "Send a reading from a sensor",
            description = "Called by the device, not by a user: it authenticates with the sensor's key in the "
                    + "X-Device-Key header (TS-01). Returns 201 for a new reading and 200 if the sourceKey was "
                    + "already received. 404 unknown sensor, 401 wrong key, 400 invalid data or unit, "
                    + "409 asset deactivated, 429 readings too frequent. If the value is outside the sensor's "
                    + "threshold the answer says so and, when it applies, an alert is raised.")
    public ResponseEntity<ReadingAcceptedResource> ingestReading(
            @RequestHeader(value = "X-Device-Key", required = false) String deviceKey,
            @Valid @RequestBody IngestReadingResource resource) {
        IngestReadingResult result = readingCommandService.handle(new IngestReadingCommand(
                resource.sensorId(), deviceKey, resource.sourceKey(), resource.value(),
                resource.unit(), resource.measuredAt()));
        return ResponseEntity.status(result.created() ? HttpStatus.CREATED : HttpStatus.OK)
                .body(ReadingAcceptedResourceAssembler.toResource(result));
    }

    @PutMapping("/{sensorId}/threshold")
    @PreAuthorize("hasRole('MAINTENANCE_MANAGER')")
    @SecurityRequirement(name = "bearerAuth")
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