package pe.edu.upc.predictivemaintain.maintenance.interfaces.rest.transform;

import pe.edu.upc.predictivemaintain.maintenance.application.queryservices.AssetEvent;
import pe.edu.upc.predictivemaintain.maintenance.application.queryservices.AvailabilityReport;
import pe.edu.upc.predictivemaintain.maintenance.application.queryservices.ComparisonRow;
import pe.edu.upc.predictivemaintain.maintenance.application.queryservices.OeeReport;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.aggregates.DowntimeInterval;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.aggregates.ProductionWindow;
import pe.edu.upc.predictivemaintain.maintenance.interfaces.rest.resources.AssetEventResource;
import pe.edu.upc.predictivemaintain.maintenance.interfaces.rest.resources.AvailabilityReportResource;
import pe.edu.upc.predictivemaintain.maintenance.interfaces.rest.resources.ComparisonRowResource;
import pe.edu.upc.predictivemaintain.maintenance.interfaces.rest.resources.DowntimeResource;
import pe.edu.upc.predictivemaintain.maintenance.interfaces.rest.resources.OeeReportResource;
import pe.edu.upc.predictivemaintain.maintenance.interfaces.rest.resources.ProductionWindowResource;

public final class ReportResourceAssembler {

    private ReportResourceAssembler() {
    }

    public static AssetEventResource toResource(AssetEvent event) {
        return new AssetEventResource(event.type(), event.referenceId(), event.occurredAt(), event.endedAt(),
                event.severity(), event.status());
    }

    public static AvailabilityReportResource toResource(AvailabilityReport report) {
        return new AvailabilityReportResource(report.assetId(), report.assetCode(), report.period().from(),
                report.period().to(), report.availabilityPercent(), report.downtimeSeconds(),
                report.periodSeconds(), !report.events().isEmpty(),
                report.events().stream().map(ReportResourceAssembler::toResource).toList());
    }

    public static ComparisonRowResource toResource(ComparisonRow row) {
        return new ComparisonRowResource(row.assetId(), row.assetCode(), row.assetName(), row.alerts(),
                row.criticalAlerts(), row.stops(), row.downtimeSeconds(), row.availabilityPercent());
    }

    public static OeeReportResource toResource(OeeReport report) {
        return new OeeReportResource(report.assetId(), report.assetCode(), report.period().from(),
                report.period().to(), report.windows(), report.availabilityPercent(),
                report.performancePercent(), report.qualityPercent(), report.oeePercent(),
                report.unavailableComponents());
    }

    public static DowntimeResource toResource(DowntimeInterval interval) {
        return new DowntimeResource(interval.getId(), interval.getAssetId(), interval.getStartedAt(),
                interval.getEndedAt());
    }

    public static ProductionWindowResource toResource(ProductionWindow window) {
        return new ProductionWindowResource(window.getId(), window.getAssetId(), window.getStartsAt(),
                window.getEndsAt(), window.getPlannedSeconds(), window.getOperatingSeconds(),
                window.getTotalUnits(), window.getGoodUnits(), window.getIdealCycleSeconds());
    }
}