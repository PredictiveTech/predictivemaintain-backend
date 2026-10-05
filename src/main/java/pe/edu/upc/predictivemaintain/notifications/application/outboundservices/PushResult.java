package pe.edu.upc.predictivemaintain.notifications.application.outboundservices;

public record PushResult(Status status, String detail) {

    public enum Status {
        SENT,
        /** The provider says this address no longer exists (the app was uninstalled or its data was cleared). */
        INVALID_TOKEN,
        FAILED,
        NOT_CONFIGURED
    }

    public static PushResult sent() {
        return new PushResult(Status.SENT, null);
    }

    public static PushResult invalidToken() {
        return new PushResult(Status.INVALID_TOKEN, "The device token is no longer valid and was removed");
    }

    public static PushResult failed(String detail) {
        return new PushResult(Status.FAILED, detail);
    }

    public static PushResult notConfigured() {
        return new PushResult(Status.NOT_CONFIGURED, "Push notifications are not configured on the server");
    }
}