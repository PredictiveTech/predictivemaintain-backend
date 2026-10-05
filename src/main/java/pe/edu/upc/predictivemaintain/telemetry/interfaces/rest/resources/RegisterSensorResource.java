package pe.edu.upc.predictivemaintain.telemetry.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pe.edu.upc.predictivemaintain.telemetry.domain.model.valueobjects.Metric;

public record RegisterSensorResource(@NotNull Metric metric, @NotBlank @Size(max = 20) String unit) {
}