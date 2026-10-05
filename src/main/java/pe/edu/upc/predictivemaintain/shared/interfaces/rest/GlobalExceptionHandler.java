package pe.edu.upc.predictivemaintain.shared.interfaces.rest;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import pe.edu.upc.predictivemaintain.shared.application.errors.ApplicationException;
import pe.edu.upc.predictivemaintain.shared.application.errors.CommonError;
import pe.edu.upc.predictivemaintain.shared.application.errors.ErrorCode;
import pe.edu.upc.predictivemaintain.shared.domain.exceptions.DomainConflictException;
import pe.edu.upc.predictivemaintain.shared.domain.exceptions.DomainValidationException;

import org.springframework.beans.TypeMismatchException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Converts every exception into an RFC 7807 ProblemDetail localized
 * with the Accept-Language header of the request.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final MessageSource messages;

    public GlobalExceptionHandler(MessageSource messages) {
        this.messages = messages;
    }

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ProblemDetail> handleApplication(ApplicationException ex, HttpServletRequest request) {
        ErrorCode code = ex.getErrorCode();
        return respond(problem(code, text(code.messageKey(), ex.getArguments()), request.getRequestURI()));
    }

    @ExceptionHandler(DomainValidationException.class)
    public ResponseEntity<ProblemDetail> handleDomainValidation(DomainValidationException ex,
                                                                HttpServletRequest request) {
        return respond(problem(CommonError.VALIDATION_ERROR,
                text(ex.getMessageKey(), ex.getArguments()), request.getRequestURI()));
    }

    /** A rule of the domain forbids the action in the current state (invalid transition, stale version...). */
    @ExceptionHandler(DomainConflictException.class)
    public ResponseEntity<ProblemDetail> handleDomainConflict(DomainConflictException ex,
                                                              HttpServletRequest request) {
        return respond(problem(CommonError.CONFLICT,
                text(ex.getMessageKey(), ex.getArguments()), request.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException ex,
                                                          HttpServletRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.putIfAbsent(error.getField(), error.getDefaultMessage()));
        ProblemDetail problem = problem(CommonError.VALIDATION_ERROR,
                text(CommonError.VALIDATION_ERROR.messageKey()), request.getRequestURI());
        problem.setProperty("errors", errors);
        return respond(problem);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleMalformedBody(HttpMessageNotReadableException ex,
                                                             HttpServletRequest request) {
        return respond(problem(CommonError.MALFORMED_REQUEST,
                text(CommonError.MALFORMED_REQUEST.messageKey()), request.getRequestURI()));
    }

    /** A query or path value cannot be converted: ?status=NOPE for an enum, or /assets/abc for a UUID. */
    @ExceptionHandler(TypeMismatchException.class)
    public ResponseEntity<ProblemDetail> handleTypeMismatch(TypeMismatchException ex, HttpServletRequest request) {
        ProblemDetail problem = problem(CommonError.VALIDATION_ERROR,
                text(CommonError.VALIDATION_ERROR.messageKey()), request.getRequestURI());
        if (ex instanceof MethodArgumentTypeMismatchException argument) {
            problem.setProperty("errors", Map.of(argument.getName(), text("validation.parameter.invalid")));
        }
        return respond(problem);
    }

    /** Thrown by @PreAuthorize when the authenticated user does not have the required role. */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return respond(problem(CommonError.FORBIDDEN,
                text(CommonError.FORBIDDEN.messageKey()), request.getRequestURI()));
    }

    /** Safety net for race conditions, e.g. two registrations with the same email at the same time. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetail> handleDataIntegrity(DataIntegrityViolationException ex,
                                                             HttpServletRequest request) {
        log.warn("Data integrity violation on {}: {}", request.getRequestURI(), ex.getMostSpecificCause().getMessage());
        return respond(problem(CommonError.CONFLICT,
                text(CommonError.CONFLICT.messageKey()), request.getRequestURI()));
    }

    /** Two people changed the same record at the same moment: the second one loses and must retry. */
    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ProblemDetail> handleOptimisticLock(OptimisticLockingFailureException ex,
                                                              HttpServletRequest request) {
        return respond(problem(CommonError.CONFLICT,
                text(CommonError.CONFLICT.messageKey()), request.getRequestURI()));
    }

    /** Raised by Spring when an uploaded file exceeds spring.servlet.multipart.max-file-size. */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ProblemDetail> handleUploadTooLarge(MaxUploadSizeExceededException ex,
                                                              HttpServletRequest request) {
        return respond(problem(CommonError.PAYLOAD_TOO_LARGE,
                text(CommonError.PAYLOAD_TOO_LARGE.messageKey()), request.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnexpected(Exception ex, HttpServletRequest request) {
        if (ex instanceof ErrorResponse springError) {
            // Spring MVC's own exceptions (unknown route, wrong method...) already know their status.
            ProblemDetail body = springError.getBody();
            body.setProperty("code", "HTTP_" + springError.getStatusCode().value());
            body.setInstance(URI.create(request.getRequestURI()));
            return ResponseEntity.status(springError.getStatusCode()).body(body);
        }
        log.error("Unexpected error on {}", request.getRequestURI(), ex);
        return respond(problem(CommonError.INTERNAL_ERROR,
                text(CommonError.INTERNAL_ERROR.messageKey()), request.getRequestURI()));
    }

    private ResponseEntity<ProblemDetail> respond(ProblemDetail problem) {
        return ResponseEntity.status(problem.getStatus()).body(problem);
    }

    private ProblemDetail problem(ErrorCode code, String detail, String path) {
        HttpStatus status = HttpStatus.valueOf(code.status());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(status.getReasonPhrase());
        problem.setProperty("code", code.code());
        if (path != null && !path.isBlank()) {
            problem.setInstance(URI.create(path));
        }
        return problem;
    }

    private String text(String key, Object... arguments) {
        return messages.getMessage(key, arguments, key, LocaleContextHolder.getLocale());
    }
}