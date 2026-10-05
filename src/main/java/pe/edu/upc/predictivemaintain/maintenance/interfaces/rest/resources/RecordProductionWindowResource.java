package pe.edu.upc.predictivemaintain.maintenance.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.Instant;

public record RecordProductionWindowResource(
        @NotNull Instant startsAt,
        @NotNull Instant endsAt,
        @NotNull @Positive @Schema(description = "Seconds the asset was scheduled to work") Long plannedSeconds,
        @NotNull @PositiveOrZero @Schema(description = "Seconds it really worked (planned minus stops)") Long operatingSeconds,
        @NotNull @PositiveOrZero @Schema(description = "Units produced, good or not") Long totalUnits,
        @NotNull @PositiveOrZero Long goodUnits,
        @NotNull @DecimalMin("0.001") @Schema(description = "Ideal seconds needed to produce one unit") BigDecimal idealCycleSeconds) {
}