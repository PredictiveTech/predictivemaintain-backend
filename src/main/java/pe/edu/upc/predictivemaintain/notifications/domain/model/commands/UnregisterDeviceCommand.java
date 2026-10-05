package pe.edu.upc.predictivemaintain.notifications.domain.model.commands;

import java.util.UUID;

public record UnregisterDeviceCommand(UUID tenantId, UUID userId, UUID deviceId) {
}
