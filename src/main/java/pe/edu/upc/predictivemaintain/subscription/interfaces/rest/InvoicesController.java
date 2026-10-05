package pe.edu.upc.predictivemaintain.subscription.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upc.predictivemaintain.iam.interfaces.acl.AuthenticatedUser;
import pe.edu.upc.predictivemaintain.shared.domain.model.valueobjects.PageQuery;
import pe.edu.upc.predictivemaintain.shared.interfaces.rest.resources.PagedResource;
import pe.edu.upc.predictivemaintain.subscription.application.queryservices.SubscriptionQueryService;
import pe.edu.upc.predictivemaintain.subscription.domain.model.queries.GetInvoicesQuery;
import pe.edu.upc.predictivemaintain.subscription.interfaces.rest.resources.InvoiceResource;
import pe.edu.upc.predictivemaintain.subscription.interfaces.rest.transform.SubscriptionResourceAssembler;

@RestController
@RequestMapping(value = "/api/v1/invoices", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Invoices", description = "Internal billing records of the subscription")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('MAINTENANCE_MANAGER')")
public class InvoicesController {

    private final SubscriptionQueryService queryService;

    public InvoicesController(SubscriptionQueryService queryService) {
        this.queryService = queryService;
    }

    @GetMapping
    @Operation(summary = "List the company's invoices",
            description = "Paginated, newest first. They are internal records that keep the amount and currency "
                    + "of the moment they were issued; ISSUED does not mean paid, because no payment is processed.")
    public ResponseEntity<PagedResource<InvoiceResource>> list(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var result = queryService.handle(new GetInvoicesQuery(principal.tenantId(), new PageQuery(page, size)));
        return ResponseEntity.ok(PagedResource.from(result, SubscriptionResourceAssembler::toResource));
    }
}