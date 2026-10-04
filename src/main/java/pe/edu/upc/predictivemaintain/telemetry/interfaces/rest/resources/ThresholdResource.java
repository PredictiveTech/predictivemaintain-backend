package pe.edu.upc.predictivemaintain.telemetry.interfaces.rest.resources;

import pe.edu.upc.predictivemaintain.telemetry.domain.model.valueobjects.ThresholdSeverity;

import java.math.BigDecimal;
import java.util.UUID;

public record ThresholdResource(UUID sensorId, BigDecimal lowerBound, BigDecimal upperBound,
                                ThresholdSeverity severity, int version) {
}