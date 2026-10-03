package pe.edu.upc.predictivemaintain.iam.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upc.predictivemaintain.iam.application.commandservices.UserAccountCommandService;
import pe.edu.upc.predictivemaintain.iam.application.queryservices.UserAccountQueryService;
import pe.edu.upc.predictivemaintain.iam.domain.model.aggregates.UserAccount;
import pe.edu.upc.predictivemaintain.iam.domain.model.commands.DeactivateUserCommand;
import pe.edu.upc.predictivemaintain.iam.domain.model.queries.GetUserAccountByIdQuery;
import pe.edu.upc.predictivemaintain.iam.interfaces.acl.AuthenticatedUser;
import pe.edu.upc.predictivemaintain.iam.interfaces.rest.resources.ChangeRolesResource;
import pe.edu.upc.predictivemaintain.iam.interfaces.rest.resources.CreateUserResource;
import pe.edu.upc.predictivemaintain.iam.interfaces.rest.resources.UpdateProfileResource;
import pe.edu.upc.predictivemaintain.iam.interfaces.rest.resources.UserResource;
import pe.edu.upc.predictivemaintain.iam.interfaces.rest.transform.IamCommandFromResourceAssembler;
import pe.edu.upc.predictivemaintain.iam.interfaces.rest.transform.UserResourceFromEntityAssembler;

import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/users", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Users", description = "Own profile and user administration inside the authenticated company")
@SecurityRequirement(name = "bearerAuth")
public class UsersController {

    private final UserAccountCommandService commandService;
    private final UserAccountQueryService queryService;

    public UsersController(UserAccountCommandService commandService, UserAccountQueryService queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    @GetMapping("/me")
    @Operation(summary = "Get my profile")
    public ResponseEntity<UserResource> getMe(@AuthenticationPrincipal AuthenticatedUser principal) {
        UserAccount user = queryService.handle(new GetUserAccountByIdQuery(principal.userId()));
        return ResponseEntity.ok(UserResourceFromEntityAssembler.toResource(user));
    }

    @PatchMapping("/me")
    @Operation(summary = "Update my profile", description = "Changes the display name (US-23).")
    public ResponseEntity<UserResource> updateMe(@AuthenticationPrincipal AuthenticatedUser principal,
                                                 @Valid @RequestBody UpdateProfileResource resource) {
        UserAccount user = commandService.handle(
                IamCommandFromResourceAssembler.toCommand(principal.userId(), resource));
        return ResponseEntity.ok(UserResourceFromEntityAssembler.toResource(user));
    }

    @PostMapping
    @PreAuthorize("hasRole('MAINTENANCE_MANAGER')")
    @Operation(summary = "Create a user in my company", description = "Only for maintenance managers.")
    public ResponseEntity<UserResource> createUser(@AuthenticationPrincipal AuthenticatedUser principal,
                                                   @Valid @RequestBody CreateUserResource resource) {
        UserAccount user = commandService.handle(
                IamCommandFromResourceAssembler.toCommand(principal.tenantId(), resource));
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResourceFromEntityAssembler.toResource(user));
    }

    @PutMapping("/{userId}/roles")
    @PreAuthorize("hasRole('MAINTENANCE_MANAGER')")
    @Operation(summary = "Change the roles of a user in my company",
            description = "Only for maintenance managers. The company must keep at least one active manager.")
    public ResponseEntity<UserResource> changeRoles(@AuthenticationPrincipal AuthenticatedUser principal,
                                                    @PathVariable UUID userId,
                                                    @Valid @RequestBody ChangeRolesResource resource) {
        UserAccount user = commandService.handle(
                IamCommandFromResourceAssembler.toCommand(principal.tenantId(), userId, resource));
        return ResponseEntity.ok(UserResourceFromEntityAssembler.toResource(user));
    }

    @PostMapping("/{userId}/deactivate")
    @PreAuthorize("hasRole('MAINTENANCE_MANAGER')")
    @Operation(summary = "Deactivate a user in my company",
            description = "Only for maintenance managers. The user can no longer log in or use the API.")
    public ResponseEntity<UserResource> deactivate(@AuthenticationPrincipal AuthenticatedUser principal,
                                                   @PathVariable UUID userId) {
        UserAccount user = commandService.handle(new DeactivateUserCommand(principal.tenantId(), userId));
        return ResponseEntity.ok(UserResourceFromEntityAssembler.toResource(user));
    }
}