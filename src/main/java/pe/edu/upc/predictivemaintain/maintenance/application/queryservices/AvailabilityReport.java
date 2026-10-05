package pe.edu.upc.predictivemaintain.maintenance.application.queryservices;

import pe.edu.upc.predictivemaintain.maintenance.domain.model.valueobjects.ReportPeriod;

import java.util.List;
import java.util.UUID;

public record AvailabilityReport(UUID assetId, String assetCode, ReportPeriod period, double availabilityPercent,
                                 long downtimeSeconds, long periodSeconds, List<AssetEvent> events) {
}