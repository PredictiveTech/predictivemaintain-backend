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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upc.predictivemaintain.iam.interfaces.acl.AuthenticatedUser;
import pe.edu.upc.predictivemaintain.telemetry.application.commandservices.RegisteredSensor;
import pe.edu.upc.predictivemaintain.telemetry.application.commandservices.SensorCommandService;
import pe.edu.upc.predictivemaintain.telemetry.domain.model.commands.RegisterSensorCommand;
import pe.edu.upc.predictivemaintain.telemetry.interfaces.rest.resources.RegisterSensorResource;
import pe.edu.upc.predictivemaintain.telemetry.interfaces.rest.resources.SensorRegisteredResource;
import pe.edu.upc.predictivemaintain.telemetry.interfaces.rest.transform.SensorResourceFromEntityAssembler;

import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/assets/{assetId}", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Telemetry", description = "Sensors, readings and thresholds of the company's assets")
@SecurityRequirement(name = "bearerAuth")
public class AssetTelemetryController {

    private final SensorCommandService sensorCommandService;

    public AssetTelemetryController(SensorCommandService sensorCommandService) {
        this.sensorCommandService = sensorCommandService;
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
}