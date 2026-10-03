package pe.edu.upc.predictivemaintain.iam.domain.model.commands;

import java.util.UUID;

public record UpdateProfileCommand(UUID userId, String displayName) {
}