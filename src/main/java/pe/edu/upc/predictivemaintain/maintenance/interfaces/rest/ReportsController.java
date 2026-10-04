package pe.edu.upc.predictivemaintain.maintenance.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upc.predictivemaintain.iam.interfaces.acl.AuthenticatedUser;
import pe.edu.upc.predictivemaintain.maintenance.application.queryservices.ReportQueryService;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.queries.GetAvailabilityReportQuery;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.queries.GetComparisonReportQuery;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.queries.GetHistoryExportQuery;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.queries.GetOeeReportQuery;
import pe.edu.upc.predictivemaintain.maintenance.interfaces.rest.resources.AvailabilityReportResource;
import pe.edu.upc.predictivemaintain.maintenance.interfaces.rest.resources.ComparisonRowResource;
import pe.edu.upc.predictivemaintain.maintenance.interfaces.rest.resources.OeeReportResource;
import pe.edu.upc.predictivemaintain.maintenance.interfaces.rest.transform.ReportCsvAssembler;
import pe.edu.upc.predictivemaintain.maintenance.interfaces.rest.transform.ReportResourceAssembler;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/reports", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Reports", description = "Availability, comparison, history export and OEE (maintenance managers)")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('MAINTENANCE_MANAGER')")
public class ReportsController {

    private final ReportQueryService reportQueryService;

    public ReportsController(ReportQueryService reportQueryService) {
        this.reportQueryService = reportQueryService;
    }

    @GetMapping("/availability")
    @Operation(summary = "History and availability of an asset",
            description = "Alerts and stops of the asset in the range, and the percentage of time it was not "
                    + "stopped (US-14, TS-07). Without events the availability is 100 and the list is empty. "
                    + "The range cannot exceed 366 days and its end never goes past now.")
    public ResponseEntity<AvailabilityReportResource> availability(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @RequestParam UUID assetId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        var report = reportQueryService.handle(new GetAvailabilityReportQuery(principal.tenantId(), assetId, from, to));
        return ResponseEntity.ok(ReportResourceAssembler.toResource(report));
    }

    @GetMapping("/comparison")
    @Operation(summary = "Compare several assets",
            description = "One row per asset with alerts, stops and availability, side by side (US-15). Between 1 "
                    + "and 20 assets, separated by commas. An asset without events appears with zeros and 100%.")
    public ResponseEntity<List<ComparisonRowResource>> comparison(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @RequestParam List<UUID> assetIds,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        var rows = reportQueryService.handle(new GetComparisonReportQuery(principal.tenantId(), assetIds, from, to));
        return ResponseEntity.ok(rows.stream().map(ReportResourceAssembler::toResource).toList());
    }

    @GetMapping(value = "/history/export", produces = "text/csv")
    @Operation(summary = "Export the history as CSV",
            description = "Downloads the alerts and stops of one or more assets as a CSV file (US-16). "
                    + "If there is nothing to export the answer is 404 with an explanatory message.")
    public ResponseEntity<byte[]> exportHistory(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @RequestParam List<UUID> assetIds,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        var rows = reportQueryService.handle(new GetHistoryExportQuery(principal.tenantId(), assetIds, from, to));
        byte[] csv = ReportCsvAssembler.toCsv(rows).getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("maintenance-history.csv").build().toString())
                .body(csv);
    }

    @GetMapping("/oee")
    @Operation(summary = "OEE of an asset",
            description = "Overall Equipment Effectiveness = availability x performance x quality, computed from "
                    + "the production windows that start in the range (US-31, TS-12). A component that cannot be "
                    + "computed is null and listed in unavailableComponents; the OEE is null unless all three exist.")
    public ResponseEntity<OeeReportResource> oee(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @RequestParam UUID assetId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        var report = reportQueryService.handle(new GetOeeReportQuery(principal.tenantId(), assetId, from, to));
        return ResponseEntity.ok(ReportResourceAssembler.toResource(report));
    }
}