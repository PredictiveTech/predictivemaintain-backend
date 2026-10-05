package pe.edu.upc.predictivemaintain.iam.application.internal.support;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Generates password recovery tokens and hashes them for storage.
 * Only the SHA-256 hash is saved, so a database leak does not expose usable links.
 */
public final class ResetTokenSupport {

    private static final SecureRandom RANDOM = new SecureRandom();

    private ResetTokenSupport() {
    }

    /** 256 random bits, URL-safe. */
    public static String generate() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public static String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }
}