package pe.edu.upc.predictivemaintain.maintenance.domain.model.commands;

import java.time.Instant;
import java.util.UUID;

public record RecordDowntimeCommand(UUID tenantId, UUID assetId, Instant startedAt, Instant endedAt) {
}