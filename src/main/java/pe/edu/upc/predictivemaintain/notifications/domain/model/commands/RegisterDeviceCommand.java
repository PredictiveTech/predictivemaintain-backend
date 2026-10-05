package pe.edu.upc.predictivemaintain.notifications.domain.model.commands;

import pe.edu.upc.predictivemaintain.notifications.domain.model.valueobjects.DevicePlatform;

import java.util.UUID;

public record RegisterDeviceCommand(UUID tenantId, UUID userId, String token, DevicePlatform platform) {
}