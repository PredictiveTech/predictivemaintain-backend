package pe.edu.upc.predictivemaintain.iam.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pe.edu.upc.predictivemaintain.iam.application.outboundservices.IssuedToken;
import pe.edu.upc.predictivemaintain.iam.application.outboundservices.TokenIssuer;
import pe.edu.upc.predictivemaintain.iam.domain.model.aggregates.UserAccount;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

/**
 * Issues and reads signed JWTs (HS256). Claims: sub (user id), tenantId, roles, iat, exp.
 */
@Component
public class JwtTokenAdapter implements TokenIssuer {

    private final SecretKey key;
    private final long expirationMinutes;
    private final Clock clock;

    public JwtTokenAdapter(@Value("${app.jwt.secret}") String secret,
                           @Value("${app.jwt.expiration-minutes:60}") long expirationMinutes,
                           Clock clock) {
        byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < 32) {
            throw new IllegalStateException("app.jwt.secret must have at least 32 characters");
        }
        this.key = Keys.hmacShaKeyFor(secretBytes);
        this.expirationMinutes = expirationMinutes;
        this.clock = clock;
    }

    @Override
    public IssuedToken issue(UserAccount user) {
        Instant now = clock.instant();
        Instant expiresAt = now.plus(Duration.ofMinutes(expirationMinutes));
        String token = Jwts.builder()
                .subject(user.getId().toString())
                .claim("tenantId", user.getTenantId().toString())
                .claim("roles", user.getRoles().stream().map(Enum::name).sorted().toList())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(key)
                .compact();
        return new IssuedToken(token, expiresAt);
    }

    /**
     * Validates signature and expiry and returns the user id, or empty if the token is not valid.
     */
    public Optional<UUID> parseUserId(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
            return Optional.of(UUID.fromString(claims.getSubject()));
        } catch (JwtException | IllegalArgumentException ex) {
            return Optional.empty();
        }
    }
}