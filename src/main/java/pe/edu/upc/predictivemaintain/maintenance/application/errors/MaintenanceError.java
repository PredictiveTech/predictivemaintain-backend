package pe.edu.upc.predictivemaintain.maintenance.application.errors;

import pe.edu.upc.predictivemaintain.shared.application.errors.ErrorCode;

/**
 * Expected failures of the Maintenance Operations context.
 */
public enum MaintenanceError implements ErrorCode {

    ASSET_NOT_FOUND(404, "error.maintenance.asset-not-found"),
    ASSET_CODE_ALREADY_EXISTS(409, "error.maintenance.asset-code-exists"),
    ASSET_INACTIVE(409, "error.maintenance.asset-inactive"),
    ASSET_HAS_OPEN_WORK_ORDERS(409, "error.maintenance.asset-has-open-orders"),
    ALERT_NOT_FOUND(404, "error.maintenance.alert-not-found"),
    ALERT_NOT_CONFIRMED(409, "error.maintenance.alert-not-confirmed"),
    WORK_ORDER_NOT_FOUND(404, "error.maintenance.work-order-not-found"),
    WORK_ORDER_ALREADY_EXISTS(409, "error.maintenance.work-order-exists"),
    INVALID_TECHNICIAN(400, "error.maintenance.invalid-technician"),
    NOT_ASSIGNED_TECHNICIAN(403, "error.maintenance.not-assigned"),
    WORK_ORDER_NOT_IN_PROGRESS(409, "error.maintenance.work-order-not-in-progress"),
    UNSUPPORTED_IMAGE(400, "error.maintenance.unsupported-image"),
    ATTACHMENT_NOT_FOUND(404, "error.maintenance.attachment-not-found");

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