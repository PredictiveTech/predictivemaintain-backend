package pe.edu.upc.predictivemaintain.notifications.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upc.predictivemaintain.iam.interfaces.acl.AuthenticatedUser;
import pe.edu.upc.predictivemaintain.notifications.application.queryservices.NotificationQueryService;
import pe.edu.upc.predictivemaintain.notifications.domain.model.queries.GetMyNotificationsQuery;
import pe.edu.upc.predictivemaintain.notifications.interfaces.rest.resources.NotificationResource;
import pe.edu.upc.predictivemaintain.notifications.interfaces.rest.transform.NotificationResourceAssembler;
import pe.edu.upc.predictivemaintain.shared.domain.model.valueobjects.PageQuery;
import pe.edu.upc.predictivemaintain.shared.interfaces.rest.resources.PagedResource;

@RestController
@RequestMapping(value = "/api/v1/notifications", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Notifications", description = "The record of what was sent to the logged-in user")
@SecurityRequirement(name = "bearerAuth")
public class NotificationsController {

    private final NotificationQueryService queryService;

    public NotificationsController(NotificationQueryService queryService) {
        this.queryService = queryService;
    }

    @GetMapping
    @Operation(summary = "My notifications",
            description = "TS-08: the record of every notification attempt to the logged-in user, newest first, "
                    + "with its status: SENT, FAILED or SKIPPED (and the reason). Open to every role; each person "
                    + "sees only their own.")
    public ResponseEntity<PagedResource<NotificationResource>> mine(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var result = queryService.handle(
                new GetMyNotificationsQuery(principal.tenantId(), principal.userId(), new PageQuery(page, size)));
        return ResponseEntity.ok(PagedResource.from(result, NotificationResourceAssembler::toResource));
    }
}