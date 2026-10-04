package pe.edu.upc.predictivemaintain.maintenance.interfaces.rest.resources;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.valueobjects.Criticality;

import java.util.UUID;

public record CreateAssetResource(
        @NotBlank @Size(max = 50) String code,
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Size(max = 120) String location,
        @NotBlank @Size(max = 80) String assetType,
        @NotNull Criticality criticality,
        UUID plantId,
        @Size(max = 100) String productionLine,
        @DecimalMin("-90.0") @DecimalMax("90.0") Double latitude,
        @DecimalMin("-180.0") @DecimalMax("180.0") Double longitude) {
}