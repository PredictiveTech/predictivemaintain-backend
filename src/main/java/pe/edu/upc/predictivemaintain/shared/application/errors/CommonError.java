package pe.edu.upc.predictivemaintain.shared.application.errors;

/**
 * Errors that are not tied to a single bounded context.
 * Each context defines its own enum implementing {@link ErrorCode}.
 */
public enum CommonError implements ErrorCode {

    VALIDATION_ERROR(400, "error.validation"),
    MALFORMED_REQUEST(400, "error.malformed-request"),
    RESOURCE_NOT_FOUND(404, "error.resource-not-found"),
    INTERNAL_ERROR(500, "error.internal");

    private final int status;
    private final String messageKey;

    CommonError(int status, String messageKey) {
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