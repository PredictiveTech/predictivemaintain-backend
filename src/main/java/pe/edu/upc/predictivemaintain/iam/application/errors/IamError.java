package pe.edu.upc.predictivemaintain.iam.application.errors;

import pe.edu.upc.predictivemaintain.shared.application.errors.ErrorCode;

/**
 * Expected failures of the Identity & Access Management context.
 */
public enum IamError implements ErrorCode {

    INVALID_CREDENTIALS(401, "error.iam.invalid-credentials"),
    ACCOUNT_DISABLED(403, "error.iam.account-disabled"),
    ACCOUNT_ALREADY_EXISTS(409, "error.iam.account-already-exists"),
    ACCOUNT_NOT_FOUND(404, "error.iam.account-not-found"),
    REGISTRATION_ID_ALREADY_USED(409, "error.iam.registration-id-used"),
    RESET_TOKEN_INVALID(400, "error.iam.reset-token-invalid"),
    USER_NOT_FOUND(404, "error.iam.user-not-found"),
    LAST_MANAGER_REQUIRED(409, "error.iam.last-manager");

    private final int status;
    private final String messageKey;

    IamError(int status, String messageKey) {
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