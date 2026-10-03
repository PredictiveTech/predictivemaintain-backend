package pe.edu.upc.predictivemaintain.iam.interfaces.acl;

import pe.edu.upc.predictivemaintain.iam.domain.model.valueobjects.RoleName;

import java.util.Set;
import java.util.UUID;

/**
 * Identity of the user making the request, as validated by the security filter.
 * Every context takes the tenantId from here and never from the request body.
 */
public record AuthenticatedUser(UUID userId, UUID tenantId, Set<RoleName> roles) {
}