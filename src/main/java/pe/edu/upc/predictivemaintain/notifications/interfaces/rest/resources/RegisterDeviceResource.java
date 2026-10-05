package pe.edu.upc.predictivemaintain.notifications.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pe.edu.upc.predictivemaintain.notifications.domain.model.valueobjects.DevicePlatform;

public record RegisterDeviceResource(
        @NotBlank @Size(max = 512)
        @Schema(description = "The FCM registration token the app got from Firebase")
        String token,
        @NotNull DevicePlatform platform) {
}