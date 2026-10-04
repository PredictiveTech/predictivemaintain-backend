package pe.edu.upc.predictivemaintain.maintenance.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.valueobjects.WorkOrderStatus;

public record UpdateWorkOrderStatusResource(
        @NotNull
        @Schema(description = "IN_PROGRESS, COMPLETED or CANCELLED")
        WorkOrderStatus status,
        @Size(max = 2000)
        @Schema(description = "Corrective actions (required for COMPLETED) or reason (required for CANCELLED)")
        String summary,
        @Schema(description = "Optional: version the client saw; a mismatch returns 409")
        Long expectedVersion) {
}