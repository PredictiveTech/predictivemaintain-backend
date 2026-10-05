package pe.edu.upc.predictivemaintain.notifications.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pe.edu.upc.predictivemaintain.notifications.domain.model.valueobjects.NotificationChannel;

import java.util.UUID;

public record CreateNotificationRuleResource(
        @NotNull @Schema(description = "The person who receives the alerts") UUID userId,
        @Size(max = 60) @Schema(description = "Kind of asset (for example PUMP). Leave empty for every kind")
        String assetType,
        @NotNull @Schema(description = "PUSH or EMAIL") NotificationChannel channel) {
}