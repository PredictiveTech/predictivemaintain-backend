package pe.edu.upc.predictivemaintain.telemetry.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upc.predictivemaintain.iam.interfaces.acl.AuthenticatedUser;
import pe.edu.upc.predictivemaintain.shared.domain.model.valueobjects.PageQuery;
import pe.edu.upc.predictivemaintain.shared.interfaces.rest.resources.PagedResource;
import pe.edu.upc.predictivemaintain.telemetry.application.commandservices.RegisteredSensor;
import pe.edu.upc.predictivemaintain.telemetry.application.commandservices.SensorCommandService;
import pe.edu.upc.predictivemaintain.telemetry.application.queryservices.AssetReadings;
import pe.edu.upc.predictivemaintain.telemetry.application.queryservices.TelemetryQueryService;
import pe.edu.upc.predictivemaintain.telemetry.domain.model.commands.RegisterSensorCommand;
import pe.edu.upc.predictivemaintain.telemetry.domain.model.queries.GetAssetReadingsQuery;
import pe.edu.upc.predictivemaintain.telemetry.domain.model.queries.GetAssetSensorsQuery;
import pe.edu.upc.predictivemaintain.telemetry.interfaces.rest.resources.ReadingResource;
import pe.edu.upc.predictivemaintain.telemetry.interfaces.rest.resources.RegisterSensorResource;
import pe.edu.upc.predictivemaintain.telemetry.interfaces.rest.resources.SensorPanelItemResource;
import pe.edu.upc.predictivemaintain.telemetry.interfaces.rest.resources.SensorRegisteredResource;
import pe.edu.upc.predictivemaintain.telemetry.interfaces.rest.transform.SensorResourceFromEntityAssembler;
import pe.edu.upc.predictivemaintain.telemetry.interfaces.rest.transform.TelemetryQueryResourceAssembler;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/assets/{assetId}", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Telemetry", description = "Sensors, readings and thresholds of the company's assets")
@SecurityRequirement(name = "bearerAuth")
public class AssetTelemetryController {

    private final SensorCommandService sensorCommandService;
    private final TelemetryQueryService telemetryQueryService;

    public AssetTelemetryController(SensorCommandService sensorCommandService,
                                    TelemetryQueryService telemetryQueryService) {
        this.sensorCommandService = sensorCommandService;
        this.telemetryQueryService = telemetryQueryService;
    }

    @PostMapping("/sensors")
    @PreAuthorize("hasRole('MAINTENANCE_MANAGER')")
    @Operation(summary = "Register a sensor on an asset",
            description = "Only for maintenance managers (US-11, scenario 2). The metric must be one of the "
                    + "supported physical variables. The answer includes the sensor's device key: it is shown "
                    + "only once, so save it.")
    public ResponseEntity<SensorRegisteredResource> registerSensor(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @PathVariable UUID assetId,
            @Valid @RequestBody RegisterSensorResource resource) {
        RegisteredSensor registered = sensorCommandService.handle(
                new RegisterSensorCommand(principal.tenantId(), assetId, resource.metric(), resource.unit()));
        return ResponseEntity.status(HttpStatus.CREATED).body(SensorResourceFromEntityAssembler.toResource(registered));
    }

    @GetMapping("/sensors")
    @Operation(summary = "Sensor panel of an asset",
            description = "For each variable: latest value, whether it is NORMAL or OUT_OF_RANGE, its threshold, "
                    + "and whether the sensor is ONLINE or has NO_COMMUNICATION (no reading for more than "
                    + "5 minutes, or never) with the time of the last reading (US-01). Open to every role.")
    public ResponseEntity<List<SensorPanelItemResource>> getSensorPanel(
            @AuthenticationPrincipal AuthenticatedUser principal, @PathVariable UUID assetId) {
        var items = telemetryQueryService.handle(new GetAssetSensorsQuery(principal.tenantId(), assetId));
        return ResponseEntity.ok(items.stream().map(TelemetryQueryResourceAssembler::toResource).toList());
    }

    @GetMapping("/readings")
    @Operation(summary = "Reading history of an asset",
            description = "Paginated, newest first. Without from/to it returns the last 24 hours; the range "
                    + "cannot exceed 31 days. Use sensorId to see a single variable. Open to every role.")
    public ResponseEntity<PagedResource<ReadingResource>> getReadings(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @PathVariable UUID assetId,
            @RequestParam(required = false) UUID sensorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        AssetReadings result = telemetryQueryService.handle(new GetAssetReadingsQuery(
                principal.tenantId(), assetId, sensorId, from, to, new PageQuery(page, size)));
        return ResponseEntity.ok(PagedResource.from(result.page(),
                reading -> TelemetryQueryResourceAssembler.toResource(reading, result.sensors())));
    }
}