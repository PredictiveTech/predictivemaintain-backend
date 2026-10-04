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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upc.predictivemaintain.iam.interfaces.acl.AuthenticatedUser;
import pe.edu.upc.predictivemaintain.maintenance.application.commandservices.AssetCommandService;
import pe.edu.upc.predictivemaintain.maintenance.application.queryservices.AssetQueryService;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.aggregates.Asset;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.commands.DeactivateAssetCommand;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.queries.GetAllAssetsQuery;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.queries.GetAssetByIdQuery;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.queries.GetAssetWeatherQuery;
import pe.edu.upc.predictivemaintain.maintenance.interfaces.rest.resources.AssetResource;
import pe.edu.upc.predictivemaintain.maintenance.interfaces.rest.resources.CreateAssetResource;
import pe.edu.upc.predictivemaintain.maintenance.interfaces.rest.resources.UpdateAssetResource;
import pe.edu.upc.predictivemaintain.maintenance.interfaces.rest.resources.WeatherResource;
import pe.edu.upc.predictivemaintain.maintenance.interfaces.rest.transform.AssetCommandFromResourceAssembler;
import pe.edu.upc.predictivemaintain.maintenance.interfaces.rest.transform.AssetResourceFromEntityAssembler;
import pe.edu.upc.predictivemaintain.maintenance.interfaces.rest.transform.WeatherResourceFromResultAssembler;
import pe.edu.upc.predictivemaintain.shared.domain.model.valueobjects.PageQuery;
import pe.edu.upc.predictivemaintain.shared.interfaces.rest.resources.PagedResource;

import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/assets", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Assets", description = "Industrial asset inventory of the authenticated company")
@SecurityRequirement(name = "bearerAuth")
public class AssetsController {

    private final AssetCommandService assetCommandService;
    private final AssetQueryService assetQueryService;

    public AssetsController(AssetCommandService assetCommandService, AssetQueryService assetQueryService) {
        this.assetCommandService = assetCommandService;
        this.assetQueryService = assetQueryService;
    }

    @PostMapping
    @PreAuthorize("hasRole('MAINTENANCE_MANAGER')")
    @Operation(summary = "Register an asset",
            description = "Reserves one slot of the plan's asset limit (US-11, TS-05). "
                    + "Returns 409 if the code already exists or the plan is full.")
    public ResponseEntity<AssetResource> register(@AuthenticationPrincipal AuthenticatedUser principal,
                                                  @Valid @RequestBody CreateAssetResource resource) {
        Asset asset = assetCommandService.handle(
                AssetCommandFromResourceAssembler.toCommand(principal.tenantId(), resource));
        return ResponseEntity.status(HttpStatus.CREATED).body(AssetResourceFromEntityAssembler.toResource(asset));
    }

    @GetMapping
    @Operation(summary = "List assets",
            description = "Paginated list of the company's assets (US-02). Inactive assets are hidden "
                    + "unless includeInactive=true. Open to every role.")
    public ResponseEntity<PagedResource<AssetResource>> list(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @RequestParam(required = false) String productionLine,
            @RequestParam(required = false) String assetType,
            @RequestParam(defaultValue = "false") boolean includeInactive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var result = assetQueryService.handle(new GetAllAssetsQuery(
                principal.tenantId(), productionLine, assetType, includeInactive, new PageQuery(page, size)));
        return ResponseEntity.ok(PagedResource.from(result, AssetResourceFromEntityAssembler::toResource));
    }

    @GetMapping("/{assetId}")
    @Operation(summary = "Get an asset", description = "Open to every role.")
    public ResponseEntity<AssetResource> getById(@AuthenticationPrincipal AuthenticatedUser principal,
                                                 @PathVariable UUID assetId) {
        Asset asset = assetQueryService.handle(new GetAssetByIdQuery(principal.tenantId(), assetId));
        return ResponseEntity.ok(AssetResourceFromEntityAssembler.toResource(asset));
    }

    @PutMapping("/{assetId}")
    @PreAuthorize("hasRole('MAINTENANCE_MANAGER')")
    @Operation(summary = "Update an asset",
            description = "Changes technical data and criticality (US-12, TS-05). The code cannot change "
                    + "and an inactive asset cannot be edited.")
    public ResponseEntity<AssetResource> update(@AuthenticationPrincipal AuthenticatedUser principal,
                                                @PathVariable UUID assetId,
                                                @Valid @RequestBody UpdateAssetResource resource) {
        Asset asset = assetCommandService.handle(
                AssetCommandFromResourceAssembler.toCommand(principal.tenantId(), assetId, resource));
        return ResponseEntity.ok(AssetResourceFromEntityAssembler.toResource(asset));
    }

    @DeleteMapping("/{assetId}")
    @PreAuthorize("hasRole('MAINTENANCE_MANAGER')")
    @Operation(summary = "Deactivate an asset",
            description = "Logical deletion: the asset becomes inactive and its plan slot is released "
                    + "(US-13, TS-05). If the asset has open work orders the answer is 409 as a warning; "
                    + "repeat with force=true to confirm. Repeating the request returns 200 again.")
    public ResponseEntity<AssetResource> deactivate(@AuthenticationPrincipal AuthenticatedUser principal,
                                                    @PathVariable UUID assetId,
                                                    @RequestParam(defaultValue = "false") boolean force) {
        Asset asset = assetCommandService.handle(new DeactivateAssetCommand(principal.tenantId(), assetId, force));
        return ResponseEntity.ok(AssetResourceFromEntityAssembler.toResource(asset));
    }

    @GetMapping("/{assetId}/weather")
    @PreAuthorize("hasAnyRole('MAINTENANCE_MANAGER', 'TECHNICIAN')")
    @Operation(summary = "Current weather at the asset location",
            description = "Temperature and humidity from an external service (US-29). If the service fails "
                    + "the answer is still 200 with availability UNAVAILABLE.")
    public ResponseEntity<WeatherResource> getWeather(@AuthenticationPrincipal AuthenticatedUser principal,
                                                      @PathVariable UUID assetId) {
        var result = assetQueryService.handle(new GetAssetWeatherQuery(principal.tenantId(), assetId));
        return ResponseEntity.ok(WeatherResourceFromResultAssembler.toResource(result));
    }
}