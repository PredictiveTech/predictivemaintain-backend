package pe.edu.upc.predictivemaintain.iam.infrastructure.notifications;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pe.edu.upc.predictivemaintain.iam.application.outboundservices.PasswordResetNotifier;
import pe.edu.upc.predictivemaintain.iam.domain.model.valueobjects.EmailAddress;

/**
 * DEVELOPMENT implementation: writes the recovery link to the server log instead of sending an email.
 * Replace it with an SMTP implementation before real users depend on it (Part F), and never
 * log tokens in production.
 */
@Component
public class LoggingPasswordResetNotifier implements PasswordResetNotifier {

    private static final Logger log = LoggerFactory.getLogger(LoggingPasswordResetNotifier.class);

    private final String baseUrl;

    public LoggingPasswordResetNotifier(
            @Value("${app.password-reset.base-url:http://localhost:3000/reset-password}") String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Override
    public void sendResetLink(EmailAddress email, String token) {
        log.info("[DEV] Password reset link for {}: {}?token={}", email.value(), baseUrl, token);
    }
}