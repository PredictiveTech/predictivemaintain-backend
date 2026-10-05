package pe.edu.upc.predictivemaintain.notifications.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upc.predictivemaintain.iam.interfaces.acl.AuthenticatedUser;
import pe.edu.upc.predictivemaintain.notifications.application.commandservices.NotificationRuleCommandService;
import pe.edu.upc.predictivemaintain.notifications.application.queryservices.NotificationQueryService;
import pe.edu.upc.predictivemaintain.notifications.domain.model.commands.CreateNotificationRuleCommand;
import pe.edu.upc.predictivemaintain.notifications.domain.model.commands.DeleteNotificationRuleCommand;
import pe.edu.upc.predictivemaintain.notifications.domain.model.queries.GetNotificationRulesQuery;
import pe.edu.upc.predictivemaintain.notifications.interfaces.rest.resources.CreateNotificationRuleResource;
import pe.edu.upc.predictivemaintain.notifications.interfaces.rest.resources.NotificationRuleResource;
import pe.edu.upc.predictivemaintain.notifications.interfaces.rest.transform.NotificationResourceAssembler;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/notification-rules", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Notification rules", description = "Who receives the alerts, and through which channel (maintenance managers)")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('MAINTENANCE_MANAGER')")
public class NotificationRulesController {

    private final NotificationRuleCommandService commandService;
    private final NotificationQueryService queryService;

    public NotificationRulesController(NotificationRuleCommandService commandService,
                                       NotificationQueryService queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    @PostMapping
    @Operation(summary = "Configure who receives alerts",
            description = "US-06. One rule says: this person receives the alerts of this kind of asset (or of all) "
                    + "through PUSH or EMAIL. A person can have several rules. If no rule matches an alert, the "
                    + "maintenance managers receive it by push. 409 if the same rule already exists.")
    public ResponseEntity<NotificationRuleResource> create(@AuthenticationPrincipal AuthenticatedUser principal,
                                                           @Valid @RequestBody CreateNotificationRuleResource resource) {
        var rule = commandService.handle(new CreateNotificationRuleCommand(
                principal.tenantId(), resource.userId(), resource.assetType(), resource.channel()));
        return ResponseEntity.status(HttpStatus.CREATED).body(NotificationResourceAssembler.toResource(rule));
    }

    @GetMapping
    @Operation(summary = "List the notification rules of the company")
    public ResponseEntity<List<NotificationRuleResource>> list(@AuthenticationPrincipal AuthenticatedUser principal) {
        var rules = queryService.handle(new GetNotificationRulesQuery(principal.tenantId()));
        return ResponseEntity.ok(rules.stream().map(NotificationResourceAssembler::toResource).toList());
    }

    @DeleteMapping("/{ruleId}")
    @Operation(summary = "Delete a notification rule")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal AuthenticatedUser principal,
                                       @PathVariable UUID ruleId) {
        commandService.handle(new DeleteNotificationRuleCommand(principal.tenantId(), ruleId));
        return ResponseEntity.noContent().build();
    }
}