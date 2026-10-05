package pe.edu.upc.predictivemaintain.iam.interfaces.rest.resources;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AuthTokenResource(String accessToken, String tokenType, Instant expiresAt,
                                UUID userId, UUID tenantId, List<String> roles) {
}