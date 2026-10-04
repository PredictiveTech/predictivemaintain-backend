package pe.edu.upc.predictivemaintain.telemetry.application.internal.support;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Generates device keys and hashes them for storage. A device key is 256 random bits, so a fast SHA-256
 * is enough (unlike passwords, it cannot be guessed); that matters because every reading verifies it.
 */
public final class DeviceKeySupport {

    private static final SecureRandom RANDOM = new SecureRandom();

    private DeviceKeySupport() {
    }

    public static String generate() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return "pmk_" + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public static String hash(String deviceKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(deviceKey.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }
}