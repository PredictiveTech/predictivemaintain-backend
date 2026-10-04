package pe.edu.upc.predictivemaintain.telemetry.domain.model.commands;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * @param deviceKey the plain key received in the X-Device-Key header (may be null if the header is missing)
 */
public record IngestReadingCommand(UUID sensorId, String deviceKey, String sourceKey, BigDecimal value,
                                   String unit, Instant measuredAt) {
}