package pe.edu.upc.predictivemaintain.telemetry.interfaces.rest.resources;

import pe.edu.upc.predictivemaintain.telemetry.domain.model.valueobjects.Metric;
import pe.edu.upc.predictivemaintain.telemetry.domain.model.valueobjects.ThresholdSeverity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * A reading that fell outside its threshold, with the limits and rule version that were applied then.
 */
public record DetectionResource(UUID id, UUID sensorId, Metric metric, String unit, BigDecimal value,
                                ThresholdSeverity severity, BigDecimal lowerBound, BigDecimal upperBound,
                                int ruleVersion, boolean alertRaised, Instant detectedAt, Instant measuredAt) {
}