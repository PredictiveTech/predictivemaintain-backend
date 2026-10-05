package pe.edu.upc.predictivemaintain.iam.infrastructure.notifications;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pe.edu.upc.predictivemaintain.iam.application.outboundservices.PasswordResetNotifier;
import pe.edu.upc.predictivemaintain.iam.domain.model.valueobjects.EmailAddress;
import pe.edu.upc.predictivemaintain.shared.application.outboundservices.EmailDeliveryException;
import pe.edu.upc.predictivemaintain.shared.application.outboundservices.EmailSender;

/**
 * Delivers the password recovery link. With a mail server configured it sends an email. Without one, the link
 * is written in the log ONLY if app.password-reset.log-links is true (development and demos): a recovery link is
 * a temporary password, and a log is not a safe place for it.
 */
@Component
public class PasswordResetLinkNotifier implements PasswordResetNotifier {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetLinkNotifier.class);

    private final EmailSender emailSender;
    private final String baseUrl;
    private final boolean logLinks;
    private final long expirationMinutes;

    public PasswordResetLinkNotifier(
            EmailSender emailSender,
            @Value("${app.password-reset.base-url:http://localhost:3000/reset-password}") String baseUrl,
            @Value("${app.password-reset.log-links:false}") boolean logLinks,
            @Value("${app.password-reset.expiration-minutes:30}") long expirationMinutes) {
        this.emailSender = emailSender;
        this.baseUrl = baseUrl;
        this.logLinks = logLinks;
        this.expirationMinutes = expirationMinutes;
    }

    @Override
    public void sendResetLink(EmailAddress email, String token) {
        String link = baseUrl + "?token=" + token;
        if (emailSender.isConfigured()) {
            try {
                emailSender.send(email.value(), "PredictiveMaintain: recuperación de contraseña / password recovery",
                        "Recibimos una solicitud para restablecer tu contraseña.\n"
                                + "Abre este enlace (vale por " + expirationMinutes + " minutos):\n" + link + "\n\n"
                                + "Si no fuiste tú, ignora este mensaje.\n\n"
                                + "We received a request to reset your password.\n"
                                + "Open this link (valid for " + expirationMinutes + " minutes):\n" + link + "\n\n"
                                + "If it was not you, ignore this message.\n");
            } catch (EmailDeliveryException ex) {
                // The request must not fail, or it would reveal whether the address exists. The failure is logged.
                log.error("The password recovery email could not be sent", ex);
            }
        } else if (logLinks) {
            log.info("[DEV] Password reset link for {}: {}", email.value(), link);
        } else {
            log.info("A password reset was requested. No email channel is configured, so the link was not delivered.");
        }
    }
}