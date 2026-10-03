package pe.edu.upc.predictivemaintain.maintenance.application.errors;

import pe.edu.upc.predictivemaintain.shared.application.errors.ErrorCode;

/**
 * Expected failures of the Maintenance Operations context.
 */
public enum MaintenanceError implements ErrorCode {

    ASSET_NOT_FOUND(404, "error.maintenance.asset-not-found"),
    ASSET_CODE_ALREADY_EXISTS(409, "error.maintenance.asset-code-exists"),
    ASSET_INACTIVE(409, "error.maintenance.asset-inactive");

    private final int status;
    private final String messageKey;

    MaintenanceError(int status, String messageKey) {
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