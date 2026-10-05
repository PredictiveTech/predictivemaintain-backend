package pe.edu.upc.predictivemaintain.notifications.interfaces.rest.resources;

import pe.edu.upc.predictivemaintain.notifications.domain.model.valueobjects.DevicePlatform;

import java.time.Instant;
import java.util.UUID;

/**
 * @param id keep it in the app: it is what DELETE /api/v1/devices/{id} needs when the user logs out
 */
public record DeviceResource(UUID id, DevicePlatform platform, Instant registeredAt) {
}