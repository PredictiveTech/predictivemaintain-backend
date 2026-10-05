package pe.edu.upc.predictivemaintain.notifications.infrastructure.push;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import pe.edu.upc.predictivemaintain.notifications.application.outboundservices.PushMessage;
import pe.edu.upc.predictivemaintain.notifications.application.outboundservices.PushResult;
import pe.edu.upc.predictivemaintain.notifications.application.outboundservices.PushSender;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Sends push notifications through Firebase Cloud Messaging. It exists only when FIREBASE_CREDENTIALS_JSON
 * is set; otherwise {@link LoggingPushSender} is used.
 *
 * <p>The credential is the content of the "service account" key file of the Firebase project, as plain JSON
 * or encoded in Base64 (easier to paste in a platform's variables). It is a secret: never commit it, never log it.
 */
@Component
@ConditionalOnExpression("'${app.fcm.credentials-json:}' != ''")
public class FcmPushSender implements PushSender {

    private static final Logger log = LoggerFactory.getLogger(FcmPushSender.class);

    private final FirebaseMessaging messaging;
    private final String androidChannelId;

    public FcmPushSender(@Value("${app.fcm.credentials-json}") String credentials,
                         @Value("${app.fcm.android-channel-id:alerts}") String androidChannelId) {
        this.androidChannelId = androidChannelId;
        try {
            String json = credentials.trim().startsWith("{")
                    ? credentials
                    : new String(Base64.getDecoder().decode(credentials.trim()), StandardCharsets.UTF_8);
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8))))
                    .build();
            FirebaseApp app = FirebaseApp.getApps().isEmpty() ? FirebaseApp.initializeApp(options) : FirebaseApp.getInstance();
            this.messaging = FirebaseMessaging.getInstance(app);
        } catch (IOException | IllegalArgumentException ex) {
            // A set but unusable credential is a deployment mistake: better to stop at startup than to fail silently later.
            throw new IllegalStateException("FIREBASE_CREDENTIALS_JSON is not a valid Firebase service account key", ex);
        }
    }

    @Override
    public PushResult send(String deviceToken, PushMessage message) {
        Message fcmMessage = Message.builder()
                .setToken(deviceToken)
                .setNotification(Notification.builder().setTitle(message.title()).setBody(message.body()).build())
                .setAndroidConfig(AndroidConfig.builder()
                        .setPriority(AndroidConfig.Priority.HIGH)
                        .setNotification(AndroidNotification.builder().setChannelId(androidChannelId).build())
                        .build())
                .putAllData(message.data())
                .build();
        try {
            messaging.send(fcmMessage);
            return PushResult.sent();
        } catch (FirebaseMessagingException ex) {
            if (ex.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED) {
                return PushResult.invalidToken();
            }
            log.warn("FCM rejected a message: {} ({})", ex.getMessagingErrorCode(), ex.getMessage());
            return PushResult.failed("FCM error: " + ex.getMessagingErrorCode());
        } catch (RuntimeException ex) {
            log.warn("FCM could not be reached", ex);
            return PushResult.failed("FCM could not be reached");
        }
    }
}