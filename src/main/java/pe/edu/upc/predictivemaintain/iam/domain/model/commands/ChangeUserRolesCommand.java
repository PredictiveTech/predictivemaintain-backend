package pe.edu.upc.predictivemaintain.iam.domain.model.commands;

import pe.edu.upc.predictivemaintain.iam.domain.model.valueobjects.RoleName;

import java.util.Set;
import java.util.UUID;

public record ChangeUserRolesCommand(UUID tenantId, UUID userId, Set<RoleName> roles) {
}