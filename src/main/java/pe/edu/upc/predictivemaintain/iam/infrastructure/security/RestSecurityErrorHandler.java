package pe.edu.upc.predictivemaintain.iam.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.LocaleResolver;
import pe.edu.upc.predictivemaintain.shared.application.errors.CommonError;
import pe.edu.upc.predictivemaintain.shared.application.errors.ErrorCode;

import java.io.IOException;
import java.net.URI;
import java.util.Locale;

/**
 * Writes the 401 (no valid token) and 403 (not enough permissions) responses produced by the
 * security filters. They happen before any controller runs, so GlobalExceptionHandler never sees them.
 */
@Component
public class RestSecurityErrorHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper objectMapper;
    private final MessageSource messages;
    private final LocaleResolver localeResolver;

    public RestSecurityErrorHandler(ObjectMapper objectMapper, MessageSource messages, LocaleResolver localeResolver) {
        this.objectMapper = objectMapper;
        this.messages = messages;
        this.localeResolver = localeResolver;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException exception) throws IOException {
        write(request, response, CommonError.UNAUTHORIZED);
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException exception) throws IOException {
        write(request, response, CommonError.FORBIDDEN);
    }

    private void write(HttpServletRequest request, HttpServletResponse response, ErrorCode code) throws IOException {
        Locale locale = localeResolver.resolveLocale(request);
        String detail = messages.getMessage(code.messageKey(), null, code.messageKey(), locale);
        HttpStatus status = HttpStatus.valueOf(code.status());

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(status.getReasonPhrase());
        problem.setProperty("code", code.code());
        problem.setInstance(URI.create(request.getRequestURI()));

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), problem);
    }
}