package pe.edu.upc.predictivemaintain.iam.application.outboundservices;

/**
 * Port to hash and verify passwords. The algorithm lives in infrastructure.
 */
public interface PasswordHasher {

    String hash(String rawPassword);

    boolean matches(String rawPassword, String passwordHash);
}