package pe.edu.upc.predictivemaintain.maintenance.interfaces.rest;

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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import pe.edu.upc.predictivemaintain.iam.domain.model.valueobjects.RoleName;
import pe.edu.upc.predictivemaintain.iam.interfaces.acl.AuthenticatedUser;
import pe.edu.upc.predictivemaintain.maintenance.application.commandservices.WorkOrderCommandService;
import pe.edu.upc.predictivemaintain.maintenance.application.queryservices.EvidenceContent;
import pe.edu.upc.predictivemaintain.maintenance.application.queryservices.WorkOrderQueryService;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.aggregates.EvidencePhoto;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.aggregates.WorkOrder;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.commands.AssignWorkOrderCommand;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.commands.AttachEvidenceCommand;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.commands.CancelWorkOrderCommand;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.commands.CompleteWorkOrderCommand;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.commands.CreateWorkOrderCommand;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.commands.StartWorkOrderCommand;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.queries.GetAllWorkOrdersQuery;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.queries.GetEvidenceContentQuery;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.queries.GetWorkOrderByIdQuery;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.queries.GetWorkOrderEvidenceQuery;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.queries.GetWorkOrderHistoryQuery;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.valueobjects.WorkOrderStatus;
import pe.edu.upc.predictivemaintain.maintenance.interfaces.rest.resources.AssignWorkOrderResource;
import pe.edu.upc.predictivemaintain.maintenance.interfaces.rest.resources.CreateWorkOrderResource;
import pe.edu.upc.predictivemaintain.maintenance.interfaces.rest.resources.EvidenceResource;
import pe.edu.upc.predictivemaintain.maintenance.interfaces.rest.resources.StartWorkOrderResource;
import pe.edu.upc.predictivemaintain.maintenance.interfaces.rest.resources.UpdateWorkOrderStatusResource;
import pe.edu.upc.predictivemaintain.maintenance.interfaces.rest.resources.WorkOrderChangeResource;
import pe.edu.upc.predictivemaintain.maintenance.interfaces.rest.resources.WorkOrderResource;
import pe.edu.upc.predictivemaintain.maintenance.interfaces.rest.transform.WorkOrderResourceFromEntityAssembler;
import pe.edu.upc.predictivemaintain.shared.application.errors.ApplicationException;
import pe.edu.upc.predictivemaintain.shared.application.errors.CommonError;
import pe.edu.upc.predictivemaintain.shared.domain.exceptions.DomainValidationException;
import pe.edu.upc.predictivemaintain.shared.domain.model.valueobjects.PageQuery;
import pe.edu.upc.predictivemaintain.shared.interfaces.rest.resources.PagedResource;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/work-orders", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Work orders", description = "Maintenance interventions that answer confirmed alerts")
@SecurityRequirement(name = "bearerAuth")
public class WorkOrdersController {

    private final WorkOrderCommandService commandService;
    private final WorkOrderQueryService queryService;

    public WorkOrdersController(WorkOrderCommandService commandService, WorkOrderQueryService queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('MAINTENANCE_MANAGER', 'TECHNICIAN')")
    @Operation(summary = "Create a work order from a confirmed alert",
            description = "The order starts OPEN and unassigned (US-07, TS-06). 409 if the alert is not "
                    + "confirmed or already has an order.")
    public ResponseEntity<WorkOrderResource> create(@AuthenticationPrincipal AuthenticatedUser principal,
                                                    @Valid @RequestBody CreateWorkOrderResource resource) {
        WorkOrder order = commandService.handle(
                new CreateWorkOrderCommand(principal.tenantId(), principal.userId(), resource.alertId()));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(WorkOrderResourceFromEntityAssembler.toResource(order));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MAINTENANCE_MANAGER', 'TECHNICIAN')")
    @Operation(summary = "List work orders",
            description = "Paginated, newest first (US-10). Managers see every order; technicians only the "
                    + "ones assigned to them. Filter with status=OPEN to see the pending ones.")
    public ResponseEntity<PagedResource<WorkOrderResource>> list(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @RequestParam(required = false) WorkOrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var result = queryService.handle(new GetAllWorkOrdersQuery(
                principal.tenantId(), restrictionFor(principal), status, new PageQuery(page, size)));
        return ResponseEntity.ok(PagedResource.from(result, WorkOrderResourceFromEntityAssembler::toResource));
    }

    @GetMapping("/{workOrderId}")
    @PreAuthorize("hasAnyRole('MAINTENANCE_MANAGER', 'TECHNICIAN')")
    @Operation(summary = "Get a work order")
    public ResponseEntity<WorkOrderResource> getById(@AuthenticationPrincipal AuthenticatedUser principal,
                                                     @PathVariable UUID workOrderId) {
        WorkOrder order = queryService.handle(
                new GetWorkOrderByIdQuery(principal.tenantId(), restrictionFor(principal), workOrderId));
        return ResponseEntity.ok(WorkOrderResourceFromEntityAssembler.toResource(order));
    }

    @GetMapping("/{workOrderId}/history")
    @PreAuthorize("hasAnyRole('MAINTENANCE_MANAGER', 'TECHNICIAN')")
    @Operation(summary = "History of a work order",
            description = "Every change of state or technician, with date and responsible user (US-07), oldest first.")
    public ResponseEntity<List<WorkOrderChangeResource>> history(@AuthenticationPrincipal AuthenticatedUser principal,
                                                                 @PathVariable UUID workOrderId) {
        var changes = queryService.handle(
                new GetWorkOrderHistoryQuery(principal.tenantId(), restrictionFor(principal), workOrderId));
        return ResponseEntity.ok(changes.stream().map(WorkOrderResourceFromEntityAssembler::toResource).toList());
    }

    @PatchMapping("/{workOrderId}/assign")
    @PreAuthorize("hasRole('MAINTENANCE_MANAGER')")
    @Operation(summary = "Assign or reassign a work order",
            description = "Only for maintenance managers (US-08, TS-06). The user must be an active technician "
                    + "of the company. Reassigning is possible until the work starts.")
    public ResponseEntity<WorkOrderResource> assign(@AuthenticationPrincipal AuthenticatedUser principal,
                                                    @PathVariable UUID workOrderId,
                                                    @Valid @RequestBody AssignWorkOrderResource resource) {
        WorkOrder order = commandService.handle(new AssignWorkOrderCommand(principal.tenantId(),
                principal.userId(), workOrderId, resource.technicianId(), resource.expectedVersion()));
        return ResponseEntity.ok(WorkOrderResourceFromEntityAssembler.toResource(order));
    }

    @PostMapping("/{workOrderId}/start")
    @PreAuthorize("hasRole('TECHNICIAN')")
    @Operation(summary = "Start a work order",
            description = "Only the assigned technician. The order must be ASSIGNED. The body is optional.")
    public ResponseEntity<WorkOrderResource> start(@AuthenticationPrincipal AuthenticatedUser principal,
                                                   @PathVariable UUID workOrderId,
                                                   @RequestBody(required = false) StartWorkOrderResource resource) {
        Long expectedVersion = resource == null ? null : resource.expectedVersion();
        WorkOrder order = commandService.handle(new StartWorkOrderCommand(
                principal.tenantId(), principal.userId(), workOrderId, expectedVersion));
        return ResponseEntity.ok(WorkOrderResourceFromEntityAssembler.toResource(order));
    }

    @PatchMapping("/{workOrderId}/status")
    @PreAuthorize("hasAnyRole('MAINTENANCE_MANAGER', 'TECHNICIAN')")
    @Operation(summary = "Change the status of a work order",
            description = "IN_PROGRESS and COMPLETED are for the assigned technician; COMPLETED requires the "
                    + "corrective actions in summary (TS-06). CANCELLED is for managers, requires a reason in "
                    + "summary, and only works while the order is OPEN or ASSIGNED.")
    public ResponseEntity<WorkOrderResource> updateStatus(@AuthenticationPrincipal AuthenticatedUser principal,
                                                          @PathVariable UUID workOrderId,
                                                          @Valid @RequestBody UpdateWorkOrderStatusResource resource) {
        UUID tenantId = principal.tenantId();
        UUID actorId = principal.userId();
        WorkOrder order = switch (resource.status()) {
            case IN_PROGRESS -> commandService.handle(
                    new StartWorkOrderCommand(tenantId, actorId, workOrderId, resource.expectedVersion()));
            case COMPLETED -> commandService.handle(new CompleteWorkOrderCommand(
                    tenantId, actorId, workOrderId, resource.summary(), resource.expectedVersion()));
            case CANCELLED -> {
                if (!principal.roles().contains(RoleName.MAINTENANCE_MANAGER)) {
                    throw new ApplicationException(CommonError.FORBIDDEN);
                }
                yield commandService.handle(new CancelWorkOrderCommand(
                        tenantId, actorId, workOrderId, resource.summary(), resource.expectedVersion()));
            }
            default -> throw new DomainValidationException("validation.work-order.status-not-allowed");
        };
        return ResponseEntity.ok(WorkOrderResourceFromEntityAssembler.toResource(order));
    }

    @PostMapping(value = "/{workOrderId}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('TECHNICIAN')")
    @Operation(summary = "Attach a photo to a work order",
            description = "Only the assigned technician, while the order is IN_PROGRESS (US-09, TS-10). "
                    + "Send the image in the multipart part named 'file'. JPEG, PNG or WebP up to 5 MB: "
                    + "other formats get 400 and bigger files get 413.")
    public ResponseEntity<EvidenceResource> attach(@AuthenticationPrincipal AuthenticatedUser principal,
                                                   @PathVariable UUID workOrderId,
                                                   @RequestPart("file") MultipartFile file) throws IOException {
        EvidencePhoto photo = commandService.handle(new AttachEvidenceCommand(
                principal.tenantId(), principal.userId(), workOrderId, file.getBytes()));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(WorkOrderResourceFromEntityAssembler.toResource(photo));
    }

    @GetMapping("/{workOrderId}/attachments")
    @PreAuthorize("hasAnyRole('MAINTENANCE_MANAGER', 'TECHNICIAN')")
    @Operation(summary = "List the photos of a work order")
    public ResponseEntity<List<EvidenceResource>> listAttachments(
            @AuthenticationPrincipal AuthenticatedUser principal, @PathVariable UUID workOrderId) {
        var photos = queryService.handle(
                new GetWorkOrderEvidenceQuery(principal.tenantId(), restrictionFor(principal), workOrderId));
        return ResponseEntity.ok(photos.stream().map(WorkOrderResourceFromEntityAssembler::toResource).toList());
    }

    @GetMapping(value = "/{workOrderId}/attachments/{attachmentId}",
            produces = {MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE, "image/webp"})
    @PreAuthorize("hasAnyRole('MAINTENANCE_MANAGER', 'TECHNICIAN')")
    @Operation(summary = "Download a photo of a work order", description = "Returns the image file itself.")
    public ResponseEntity<byte[]> downloadAttachment(@AuthenticationPrincipal AuthenticatedUser principal,
                                                     @PathVariable UUID workOrderId,
                                                     @PathVariable UUID attachmentId) {
        EvidenceContent content = queryService.handle(new GetEvidenceContentQuery(
                principal.tenantId(), restrictionFor(principal), workOrderId, attachmentId));
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(content.mimeType()))
                .body(content.content());
    }

    /** Managers see everything; any other user (a technician) only sees what is assigned to him. */
    private static UUID restrictionFor(AuthenticatedUser principal) {
        return principal.roles().contains(RoleName.MAINTENANCE_MANAGER) ? null : principal.userId();
    }
}