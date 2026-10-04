package pe.edu.upc.predictivemaintain.maintenance.application.queryservices;

import java.util.UUID;

/**
 * One asset in the comparative report. An asset without events appears with zeros and 100% availability.
 */
public record ComparisonRow(UUID assetId, String assetCode, String assetName, int alerts, int criticalAlerts,
                            int stops, long downtimeSeconds, double availabilityPercent) {
}