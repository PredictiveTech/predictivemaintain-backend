package pe.edu.upc.predictivemaintain.maintenance.domain.model.commands;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record RecordProductionWindowCommand(UUID tenantId, UUID assetId, Instant startsAt, Instant endsAt,
                                            long plannedSeconds, long operatingSeconds, long totalUnits,
                                            long goodUnits, BigDecimal idealCycleSeconds) {
}