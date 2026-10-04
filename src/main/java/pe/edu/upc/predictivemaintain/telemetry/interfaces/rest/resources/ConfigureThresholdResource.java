package pe.edu.upc.predictivemaintain.telemetry.interfaces.rest.resources;

import jakarta.validation.constraints.NotNull;
import pe.edu.upc.predictivemaintain.telemetry.domain.model.valueobjects.ThresholdSeverity;

import java.math.BigDecimal;

public record ConfigureThresholdResource(@NotNull BigDecimal lowerBound, @NotNull BigDecimal upperBound,
                                         @NotNull ThresholdSeverity severity) {
}