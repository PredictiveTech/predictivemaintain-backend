package pe.edu.upc.predictivemaintain.maintenance.interfaces.rest.resources;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * @param hasEvents false when the asset has no alerts or stops in the period (the availability is then 100)
 */
public record AvailabilityReportResource(UUID assetId, String assetCode, Instant from, Instant to,
                                         double availabilityPercent, long downtimeSeconds, long periodSeconds,
                                         boolean hasEvents, List<AssetEventResource> events) {
}