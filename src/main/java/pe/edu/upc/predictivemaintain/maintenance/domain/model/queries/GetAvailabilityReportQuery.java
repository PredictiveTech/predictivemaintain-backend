package pe.edu.upc.predictivemaintain.maintenance.domain.model.queries;

import java.time.Instant;
import java.util.UUID;

public record GetAvailabilityReportQuery(UUID tenantId, UUID assetId, Instant from, Instant to) {
}