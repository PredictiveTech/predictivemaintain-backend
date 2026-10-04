package pe.edu.upc.predictivemaintain.maintenance.interfaces.rest.resources;

import java.util.UUID;

public record ComparisonRowResource(UUID assetId, String assetCode, String assetName, int alerts,
                                    int criticalAlerts, int stops, long downtimeSeconds,
                                    double availabilityPercent) {
}