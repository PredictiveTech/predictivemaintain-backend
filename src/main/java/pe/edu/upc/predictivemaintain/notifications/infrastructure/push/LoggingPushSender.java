package pe.edu.upc.predictivemaintain.notifications.infrastructure.push;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import pe.edu.upc.predictivemaintain.notifications.application.outboundservices.PushMessage;
import pe.edu.upc.predictivemaintain.notifications.application.outboundservices.PushResult;
import pe.edu.upc.predictivemaintain.notifications.application.outboundservices.PushSender;

/**
 * Used when Firebase is not configured: the server works, and the notification is recorded as SKIPPED.
 */
@Component
@ConditionalOnExpression("'${app.fcm.credentials-json:}' == ''")
public class LoggingPushSender implements PushSender {

    private static final Logger log = LoggerFactory.getLogger(LoggingPushSender.class);

    @Override
    public PushResult send(String deviceToken, PushMessage message) {
        log.info("Push not sent because Firebase is not configured (FIREBASE_CREDENTIALS_JSON): {}", message.title());
        return PushResult.notConfigured();
    }
}