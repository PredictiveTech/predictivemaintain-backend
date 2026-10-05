package pe.edu.upc.predictivemaintain.telemetry.interfaces.rest.resources;

import pe.edu.upc.predictivemaintain.telemetry.domain.model.valueobjects.ThresholdSeverity;

import java.util.UUID;

/**
 * @param duplicate   true when the sourceKey had already been received (nothing new was stored)
 * @param outOfRange  true when the value is outside the allowed interval
 * @param alertRaised true when this reading produced a new alert
 * @param severity    severity of the rule that was exceeded; null when the value is within range
 */
public record ReadingAcceptedResource(UUID readingId, UUID sensorId, boolean duplicate, boolean outOfRange,
                                      boolean alertRaised, ThresholdSeverity severity) {
}