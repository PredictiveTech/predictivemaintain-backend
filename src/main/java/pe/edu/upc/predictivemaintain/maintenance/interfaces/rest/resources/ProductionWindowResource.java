package pe.edu.upc.predictivemaintain.maintenance.interfaces.rest.resources;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProductionWindowResource(UUID id, UUID assetId, Instant startsAt, Instant endsAt,
                                       long plannedSeconds, long operatingSeconds, long totalUnits,
                                       long goodUnits, BigDecimal idealCycleSeconds) {
}