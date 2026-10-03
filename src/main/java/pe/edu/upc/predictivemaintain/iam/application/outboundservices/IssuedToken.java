package pe.edu.upc.predictivemaintain.iam.application.outboundservices;

import java.time.Instant;

public record IssuedToken(String accessToken, Instant expiresAt) {
}