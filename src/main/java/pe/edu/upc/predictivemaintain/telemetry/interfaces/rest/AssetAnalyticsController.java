package pe.edu.upc.predictivemaintain.telemetry.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upc.predictivemaintain.iam.interfaces.acl.AuthenticatedUser;
import pe.edu.upc.predictivemaintain.shared.domain.model.valueobjects.PageQuery;
import pe.edu.upc.predictivemaintain.telemetry.application.queryservices.AnalyticsQueryService;
import pe.edu.upc.predictivemaintain.telemetry.domain.model.queries.GetAssetAnalyticsQuery;
import pe.edu.upc.predictivemaintain.telemetry.domain.model.queries.GetAssetRulQuery;
import pe.edu.upc.predictivemaintain.telemetry.interfaces.rest.resources.AnalyticsResource;
import pe.edu.upc.predictivemaintain.telemetry.interfaces.rest.resources.RulResource;
import pe.edu.upc.predictivemaintain.telemetry.interfaces.rest.transform.AnalyticsResourceAssembler;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/assets/{assetId}", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Analytics", description = "Remaining useful life and anomaly detections (maintenance managers)")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('MAINTENANCE_MANAGER')")
public class AssetAnalyticsController {

    private final AnalyticsQueryService analyticsQueryService;

    public AssetAnalyticsController(AnalyticsQueryService analyticsQueryService) {
        this.analyticsQueryService = analyticsQueryService;
    }

    @GetMapping("/rul")
    @Operation(summary = "Remaining useful life of an asset",
            description = "Hours until the asset's variables reach their threshold limits, estimated from their "
                    + "recent trend (US-30, TS-11). With too little history or no clear trend the answer is "
                    + "still 200 but availability is UNAVAILABLE and the other values are null.")
    public ResponseEntity<RulResource> getRul(@AuthenticationPrincipal AuthenticatedUser principal,
                                              @PathVariable UUID assetId) {
        var rul = analyticsQueryService.handle(new GetAssetRulQuery(principal.tenantId(), assetId));
        return ResponseEntity.ok(AnalyticsResourceAssembler.toResource(rul));
    }

    @GetMapping("/analytics")
    @Operation(summary = "Analytics of an asset",
            description = "Paginated anomaly detections (newest first) together with the remaining useful life. "
                    + "Without from/to the last 7 days are used; the range cannot exceed 31 days.")
    public ResponseEntity<AnalyticsResource> getAnalytics(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @PathVariable UUID assetId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var analytics = analyticsQueryService.handle(new GetAssetAnalyticsQuery(
                principal.tenantId(), assetId, from, to, new PageQuery(page, size)));
        return ResponseEntity.ok(AnalyticsResourceAssembler.toResource(analytics));
    }
}