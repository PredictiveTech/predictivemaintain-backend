package pe.edu.upc.predictivemaintain.iam.domain.model.commands;

import pe.edu.upc.predictivemaintain.iam.domain.model.valueobjects.RoleName;

import java.util.Set;
import java.util.UUID;

/**
 * The tenantId comes from the authenticated identity, never from the request body.
 */
public record CreateUserCommand(UUID tenantId, String email, String displayName,
                                String initialPassword, Set<RoleName> roles) {
}