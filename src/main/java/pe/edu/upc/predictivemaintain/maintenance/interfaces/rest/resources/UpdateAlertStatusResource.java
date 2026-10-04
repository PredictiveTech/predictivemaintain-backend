package pe.edu.upc.predictivemaintain.maintenance.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.valueobjects.AlertStatus;

public record UpdateAlertStatusResource(
        @NotNull
        @Schema(description = "CONFIRMED or DISCARDED")
        AlertStatus status,
        @Size(max = 500)
        @Schema(description = "Required when the status is DISCARDED")
        String reason,
        @Schema(description = "Optional: version the client saw; a mismatch returns 409")
        Long expectedVersion) {
}