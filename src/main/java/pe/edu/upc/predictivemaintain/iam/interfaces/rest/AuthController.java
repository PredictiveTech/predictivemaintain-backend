package pe.edu.upc.predictivemaintain.iam.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upc.predictivemaintain.iam.application.commandservices.AuthenticationCommandService;
import pe.edu.upc.predictivemaintain.iam.application.commandservices.LoginResult;
import pe.edu.upc.predictivemaintain.iam.application.commandservices.RegisterCompanyAccountResult;
import pe.edu.upc.predictivemaintain.iam.interfaces.rest.resources.AuthTokenResource;
import pe.edu.upc.predictivemaintain.iam.interfaces.rest.resources.LoginResource;
import pe.edu.upc.predictivemaintain.iam.interfaces.rest.resources.PasswordResetRequestResource;
import pe.edu.upc.predictivemaintain.iam.interfaces.rest.resources.RegisterCompanyResource;
import pe.edu.upc.predictivemaintain.iam.interfaces.rest.resources.RegisteredAccountResource;
import pe.edu.upc.predictivemaintain.iam.interfaces.rest.resources.ResetPasswordResource;
import pe.edu.upc.predictivemaintain.iam.interfaces.rest.transform.AuthTokenResourceFromResultAssembler;
import pe.edu.upc.predictivemaintain.iam.interfaces.rest.transform.IamCommandFromResourceAssembler;

@RestController
@RequestMapping(value = "/api/v1/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Authentication", description = "Company registration, login and password recovery (public endpoints)")
public class AuthController {

    private final AuthenticationCommandService authenticationCommandService;

    public AuthController(AuthenticationCommandService authenticationCommandService) {
        this.authenticationCommandService = authenticationCommandService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a company account",
            description = "Creates the company, its initial subscription and its first maintenance manager "
                    + "(US-20). Returns 201 for a new registration, or 200 if the same registrationId was "
                    + "already used with the same data. Returns 409 if the email already exists.")
    public ResponseEntity<RegisteredAccountResource> register(@Valid @RequestBody RegisterCompanyResource resource) {
        RegisterCompanyAccountResult result =
                authenticationCommandService.handle(IamCommandFromResourceAssembler.toCommand(resource));
        RegisteredAccountResource body = new RegisteredAccountResource(result.tenantId(), result.userId());
        return ResponseEntity.status(result.created() ? HttpStatus.CREATED : HttpStatus.OK).body(body);
    }

    @PostMapping("/login")
    @Operation(summary = "Log in",
            description = "Returns a signed JWT with the user's roles (US-21, TS-03). "
                    + "Wrong email and wrong password get the same 401 response.")
    public ResponseEntity<AuthTokenResource> login(@Valid @RequestBody LoginResource resource) {
        LoginResult result = authenticationCommandService.handle(IamCommandFromResourceAssembler.toCommand(resource));
        return ResponseEntity.ok(AuthTokenResourceFromResultAssembler.toResource(result));
    }

    @PostMapping("/password-reset-requests")
    @Operation(summary = "Request a password recovery link",
            description = "Generates a single-use link valid for a limited time (US-22). "
                    + "Returns 404 if no active account uses that email.")
    public ResponseEntity<Void> requestPasswordReset(@Valid @RequestBody PasswordResetRequestResource resource) {
        authenticationCommandService.handle(IamCommandFromResourceAssembler.toCommand(resource));
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/password-reset")
    @Operation(summary = "Set a new password with a recovery token",
            description = "The token comes from the link sent by email and can be used only once.")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordResource resource) {
        authenticationCommandService.handle(IamCommandFromResourceAssembler.toCommand(resource));
        return ResponseEntity.noContent().build();
    }
}