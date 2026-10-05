package pe.edu.upc.predictivemaintain.notifications.application.errors;

import pe.edu.upc.predictivemaintain.shared.application.errors.ErrorCode;

/**
 * Expected failures of the Notifications context.
 */
public enum NotificationError implements ErrorCode {

    RULE_ALREADY_EXISTS(409, "error.notifications.rule-exists"),
    RULE_NOT_FOUND(404, "error.notifications.rule-not-found"),
    DEVICE_NOT_FOUND(404, "error.notifications.device-not-found"),
    RECIPIENT_NOT_VALID(400, "error.notifications.recipient-invalid");

    private final int status;
    private final String messageKey;

    NotificationError(int status, String messageKey) {
        this.status = status;
        this.messageKey = messageKey;
    }

    @Override
    public int status() {
        return status;
    }

    @Override
    public String messageKey() {
        return messageKey;
    }
}