package pe.edu.upc.predictivemaintain.maintenance.application.errors;

import pe.edu.upc.predictivemaintain.shared.application.errors.ErrorCode;

/**
 * Expected failures of the reporting use cases.
 */
public enum ReportError implements ErrorCode {

    REPORT_EMPTY(404, "error.maintenance.report-empty");

    private final int status;
    private final String messageKey;

    ReportError(int status, String messageKey) {
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