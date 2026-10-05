package pe.edu.upc.predictivemaintain.maintenance.domain.model.queries;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record GetHistoryExportQuery(UUID tenantId, List<UUID> assetIds, Instant from, Instant to) {
}