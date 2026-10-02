package pe.edu.upc.predictivemaintain.shared.application.errors;

/**
 * Exception used by application services to report an expected failure
 * (not found, conflict, forbidden...). The API layer converts it to a ProblemDetail.
 */
public class ApplicationException extends RuntimeException {

    private final ErrorCode errorCode;
    private final Object[] arguments;

    public ApplicationException(ErrorCode errorCode, Object... arguments) {
        super(errorCode.messageKey());
        this.errorCode = errorCode;
        this.arguments = arguments;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public Object[] getArguments() {
        return arguments;
    }
}