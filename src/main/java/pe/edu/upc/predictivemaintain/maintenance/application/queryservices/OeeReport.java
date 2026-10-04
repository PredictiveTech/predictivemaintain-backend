package pe.edu.upc.predictivemaintain.maintenance.application.queryservices;

import pe.edu.upc.predictivemaintain.maintenance.domain.model.valueobjects.ReportPeriod;

import java.util.List;
import java.util.UUID;

/**
 * The OEE and its three components, as percentages. A component that could not be computed is null and
 * appears in unavailableComponents; the OEE is null unless the three are available.
 */
public record OeeReport(UUID assetId, String assetCode, ReportPeriod period, int windows,
                        Double availabilityPercent, Double performancePercent, Double qualityPercent,
                        Double oeePercent, List<String> unavailableComponents) {
}