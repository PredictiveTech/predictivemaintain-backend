package pe.edu.upc.predictivemaintain.notifications.application.outboundservices;

/**
 * Port to the service that delivers push notifications to phones (Firebase Cloud Messaging today).
 */
public interface PushSender {

    /** Never throws: every outcome is a {@link PushResult}. */
    PushResult send(String deviceToken, PushMessage message);
}