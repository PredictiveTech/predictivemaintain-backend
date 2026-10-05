package pe.edu.upc.predictivemaintain.maintenance.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upc.predictivemaintain.iam.interfaces.acl.AuthenticatedUser;
import pe.edu.upc.predictivemaintain.maintenance.application.commandservices.SyncCommandService;
import pe.edu.upc.predictivemaintain.maintenance.interfaces.rest.resources.SyncRequestResource;
import pe.edu.upc.predictivemaintain.maintenance.interfaces.rest.resources.SyncResponseResource;
import pe.edu.upc.predictivemaintain.maintenance.interfaces.rest.transform.SyncResourceAssembler;

@RestController
@RequestMapping(value = "/api/v1/sync", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Offline synchronization", description = "Sends what a technician did without a connection")
@SecurityRequirement(name = "bearerAuth")
public class SyncController {

    private final SyncCommandService syncCommandService;
    private final MessageSource messages;

    public SyncController(SyncCommandService syncCommandService, MessageSource messages) {
        this.syncCommandService = syncCommandService;
        this.messages = messages;
    }

    @PostMapping("/work-orders")
    @PreAuthorize("hasRole('TECHNICIAN')")
    @Operation(summary = "Synchronize work order actions done offline",
            description = "US-28. The app sends a batch of up to 100 actions (START, COMPLETE), each with its own "
                    + "operationId and the time the technician did it. The answer is always 200 and tells, action "
                    + "by action: APPLIED, ALREADY_APPLIED (safe to retry), SUPERSEDED (the server has a more recent "
                    + "change or the order cannot take the action: the server wins, show the message to the "
                    + "technician) or REJECTED. A device clock more than 24 hours off the server's gets 400.")
    public ResponseEntity<SyncResponseResource> synchronize(@AuthenticationPrincipal AuthenticatedUser principal,
                                                            @Valid @RequestBody SyncRequestResource resource) {
        var result = syncCommandService.handle(
                SyncResourceAssembler.toCommand(principal.tenantId(), principal.userId(), resource));
        return ResponseEntity.ok(SyncResourceAssembler.toResource(result, messages));
    }
}