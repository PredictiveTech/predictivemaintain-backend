package pe.edu.upc.predictivemaintain.telemetry.domain.model.commands;

import pe.edu.upc.predictivemaintain.telemetry.domain.model.valueobjects.ThresholdSeverity;

import java.math.BigDecimal;
import java.util.UUID;

public record ConfigureThresholdCommand(UUID tenantId, UUID sensorId, BigDecimal lowerBound,
                                        BigDecimal upperBound, ThresholdSeverity severity) {
}