package pe.edu.upc.predictivemaintain.shared.infrastructure.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import pe.edu.upc.predictivemaintain.shared.application.outboundservices.EmailSender;

/**
 * Used when no mail server is configured: it says that an email would have been sent, without writing its
 * content (an email may carry a recovery link, and a log is not a safe place for it).
 */
@Component
@ConditionalOnExpression("'${app.mail.host:}' == ''")
public class LoggingEmailSender implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(LoggingEmailSender.class);

    @Override
    public boolean isConfigured() {
        return false;
    }

    @Override
    public void send(String to, String subject, String body) {
        log.info("Email not sent because no mail server is configured (MAIL_HOST). To: {} Subject: {}", to, subject);
    }
}