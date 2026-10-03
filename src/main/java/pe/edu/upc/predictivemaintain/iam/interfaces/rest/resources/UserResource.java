package pe.edu.upc.predictivemaintain.iam.interfaces.rest.resources;

import java.util.List;
import java.util.UUID;

/**
 * Public view of a user. It never includes the password or its hash.
 */
public record UserResource(UUID id, UUID tenantId, String email, String displayName,
                           boolean active, List<String> roles) {
}