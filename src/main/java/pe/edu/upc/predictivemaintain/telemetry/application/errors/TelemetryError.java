package pe.edu.upc.predictivemaintain.telemetry.application.errors;

import pe.edu.upc.predictivemaintain.shared.application.errors.ErrorCode;

/**
 * Expected failures of the Asset Telemetry & Analytics context.
 */
public enum TelemetryError implements ErrorCode {

    SENSOR_NOT_FOUND(404, "error.telemetry.sensor-not-found"),
    ASSET_NOT_FOUND(404, "error.telemetry.asset-not-found"),
    ASSET_INACTIVE(409, "error.telemetry.asset-inactive"),
    INVALID_DEVICE_CREDENTIAL(401, "error.telemetry.invalid-device-credential"),
    SENSOR_INACTIVE(409, "error.telemetry.sensor-inactive"),
    UNIT_MISMATCH(400, "error.telemetry.unit-mismatch"),
    READING_TOO_FREQUENT(429, "error.telemetry.too-frequent");

    private final int status;
    private final String messageKey;

    TelemetryError(int status, String messageKey) {
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