package pe.edu.upc.predictivemaintain.maintenance.interfaces.rest.resources;

import java.time.Instant;
import java.util.UUID;

public record DowntimeResource(UUID id, UUID assetId, Instant startedAt, Instant endedAt) {
}