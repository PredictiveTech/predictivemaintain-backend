package pe.edu.upc.predictivemaintain.iam.domain.model.commands;

import java.util.UUID;

public record DeactivateUserCommand(UUID tenantId, UUID userId) {
}