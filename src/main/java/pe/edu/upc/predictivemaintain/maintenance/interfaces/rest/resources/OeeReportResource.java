package pe.edu.upc.predictivemaintain.maintenance.interfaces.rest.resources;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * @param oeePercent            null unless availability, performance and quality could all be computed
 * @param unavailableComponents which of AVAILABILITY, PERFORMANCE and QUALITY could not be computed
 */
public record OeeReportResource(UUID assetId, String assetCode, Instant from, Instant to, int windows,
                                Double availabilityPercent, Double performancePercent, Double qualityPercent,
                                Double oeePercent, List<String> unavailableComponents) {
}