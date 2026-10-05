package pe.edu.upc.predictivemaintain.notifications.domain.model.valueobjects;

/**
 * SENT: handed over to the push service or the mail server. FAILED: it was tried and did not work.
 * SKIPPED: it could not even be tried (the person has no registered device, or the channel is not configured).
 */
public enum DeliveryStatus {
    SENT,
    FAILED,
    SKIPPED
}