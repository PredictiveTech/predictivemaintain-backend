package pe.edu.upc.predictivemaintain.maintenance.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record RecordDowntimeResource(
        @NotNull @Schema(description = "ISO-8601 instant, for example 2026-10-04T08:00:00Z") Instant startedAt,
        @NotNull Instant endedAt) {
}