package pe.edu.upc.predictivemaintain.iam.interfaces.rest.resources;

import java.util.UUID;

public record RegisteredAccountResource(UUID tenantId, UUID userId) {
}