package pe.edu.upc.predictivemaintain.maintenance.interfaces.rest;

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
import pe.edu.upc.predictivemaintain.maintenance.application.commandservices.OperationDataCommandService;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.commands.RecordDowntimeCommand;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.commands.RecordProductionWindowCommand;
import pe.edu.upc.predictivemaintain.maintenance.interfaces.rest.resources.DowntimeResource;
import pe.edu.upc.predictivemaintain.maintenance.interfaces.rest.resources.ProductionWindowResource;
import pe.edu.upc.predictivemaintain.maintenance.interfaces.rest.resources.RecordDowntimeResource;
import pe.edu.upc.predictivemaintain.maintenance.interfaces.rest.resources.RecordProductionWindowResource;
import pe.edu.upc.predictivemaintain.maintenance.interfaces.rest.transform.ReportResourceAssembler;

import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/assets/{assetId}", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Operation data", description = "Stops and production records that feed the availability and OEE reports")
@SecurityRequirement(name = "bearerAuth")
public class AssetOperationController {

    private final OperationDataCommandService operationDataCommandService;

    public AssetOperationController(OperationDataCommandService operationDataCommandService) {
        this.operationDataCommandService = operationDataCommandService;
    }

    @PostMapping("/downtime")
    @PreAuthorize("hasRole('MAINTENANCE_MANAGER')")
    @Operation(summary = "Record a stop of the asset",
            description = "Only for maintenance managers. The end must be after the start and cannot be in the "
                    + "future. Overlapping stops are allowed: the shared time is counted once in the reports.")
    public ResponseEntity<DowntimeResource> recordDowntime(@AuthenticationPrincipal AuthenticatedUser principal,
                                                           @PathVariable UUID assetId,
                                                           @Valid @RequestBody RecordDowntimeResource resource) {
        var interval = operationDataCommandService.handle(new RecordDowntimeCommand(
                principal.tenantId(), assetId, resource.startedAt(), resource.endedAt()));
        return ResponseEntity.status(HttpStatus.CREATED).body(ReportResourceAssembler.toResource(interval));
    }

    @PostMapping("/production-windows")
    @PreAuthorize("hasRole('MAINTENANCE_MANAGER')")
    @Operation(summary = "Record the production of a period",
            description = "Only for maintenance managers. Planned and operating seconds, units produced and "
                    + "good units, and the ideal seconds per unit. Impossible data (more good than produced "
                    + "units, operating time above the planned one, producing faster than the ideal cycle) "
                    + "is rejected with 400.")
    public ResponseEntity<ProductionWindowResource> recordProductionWindow(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @PathVariable UUID assetId,
            @Valid @RequestBody RecordProductionWindowResource resource) {
        var window = operationDataCommandService.handle(new RecordProductionWindowCommand(
                principal.tenantId(), assetId, resource.startsAt(), resource.endsAt(),
                resource.plannedSeconds(), resource.operatingSeconds(), resource.totalUnits(),
                resource.goodUnits(), resource.idealCycleSeconds()));
        return ResponseEntity.status(HttpStatus.CREATED).body(ReportResourceAssembler.toResource(window));
    }
}