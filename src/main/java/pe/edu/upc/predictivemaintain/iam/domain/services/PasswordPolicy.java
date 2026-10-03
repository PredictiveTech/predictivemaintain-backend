package pe.edu.upc.predictivemaintain.iam.domain.services;

import pe.edu.upc.predictivemaintain.shared.domain.exceptions.DomainValidationException;

import java.nio.charset.StandardCharsets;

/**
 * Rules every password must meet. The 72-byte limit comes from BCrypt, which ignores
 * everything after that point.
 */
public final class PasswordPolicy {

    public static final int MIN_LENGTH = 8;
    public static final int MAX_BYTES = 72;

    private PasswordPolicy() {
    }

    public static void validate(String rawPassword) {
        if (rawPassword == null
                || rawPassword.length() < MIN_LENGTH
                || rawPassword.getBytes(StandardCharsets.UTF_8).length > MAX_BYTES) {
            throw new DomainValidationException("validation.password.invalid");
        }
    }
}