package pe.edu.upc.predictivemaintain.telemetry.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record IngestReadingResource(
        @NotNull UUID sensorId,
        @NotBlank @Size(max = 100)
        @Schema(description = "Chosen by the device. Sending the same key again never stores a second reading")
        String sourceKey,
        @NotNull BigDecimal value,
        @NotBlank @Size(max = 20) String unit,
        @NotNull
        @Schema(description = "ISO-8601 instant, for example 2026-10-04T12:00:00Z")
        Instant measuredAt) {
}