package pe.edu.upc.predictivemaintain.subscription.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upc.predictivemaintain.iam.interfaces.acl.AuthenticatedUser;
import pe.edu.upc.predictivemaintain.subscription.application.commandservices.SubscriptionCommandService;
import pe.edu.upc.predictivemaintain.subscription.application.queryservices.SubscriptionQueryService;
import pe.edu.upc.predictivemaintain.subscription.domain.model.commands.ChangeSubscriptionPlanCommand;
import pe.edu.upc.predictivemaintain.subscription.domain.model.commands.RenewSubscriptionCommand;
import pe.edu.upc.predictivemaintain.subscription.domain.model.queries.GetSubscriptionQuery;
import pe.edu.upc.predictivemaintain.subscription.interfaces.rest.resources.ChangePlanResource;
import pe.edu.upc.predictivemaintain.subscription.interfaces.rest.resources.SubscriptionResource;
import pe.edu.upc.predictivemaintain.subscription.interfaces.rest.transform.SubscriptionResourceAssembler;

@RestController
@RequestMapping(value = "/api/v1/subscription", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Subscription", description = "Plan, consumption and renewal of the company's subscription")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('MAINTENANCE_MANAGER')")
public class SubscriptionController {

    private final SubscriptionCommandService commandService;
    private final SubscriptionQueryService queryService;

    public SubscriptionController(SubscriptionCommandService commandService,
                                  SubscriptionQueryService queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    @GetMapping
    @Operation(summary = "Subscription panel",
            description = "Plan, assets used versus the limit (nearLimit from 90%), and days left (expiresSoon "
                    + "with 7 days or fewer). monitoringAllowed is false once the subscription expires "
                    + "(US-17 to US-19).")
    public ResponseEntity<SubscriptionResource> getSubscription(@AuthenticationPrincipal AuthenticatedUser principal) {
        return ResponseEntity.ok(SubscriptionResourceAssembler.toResource(
                queryService.handle(new GetSubscriptionQuery(principal.tenantId()))));
    }

    @PutMapping("/plan")
    @Operation(summary = "Change the plan",
            description = "Moves to a bigger or smaller plan and issues an invoice (US-17). Returns 409 when the "
                    + "assets in use exceed the limit of the destination plan. Asking for the current plan changes "
                    + "nothing and creates no invoice.")
    public ResponseEntity<SubscriptionResource> changePlan(@AuthenticationPrincipal AuthenticatedUser principal,
                                                           @Valid @RequestBody ChangePlanResource resource) {
        commandService.handle(new ChangeSubscriptionPlanCommand(principal.tenantId(), resource.planId()));
        return ResponseEntity.ok(SubscriptionResourceAssembler.toResource(
                queryService.handle(new GetSubscriptionQuery(principal.tenantId()))));
    }

    @PostMapping("/renewal")
    @Operation(summary = "Renew the subscription",
            description = "Extends the period by one billing cycle (30 days) and issues an invoice. It starts from "
                    + "the current end date, or from now if it already expired. It cannot go more than 12 months "
                    + "ahead (409). No payment is processed: the invoice stays ISSUED.")
    public ResponseEntity<SubscriptionResource> renew(@AuthenticationPrincipal AuthenticatedUser principal) {
        commandService.handle(new RenewSubscriptionCommand(principal.tenantId()));
        return ResponseEntity.ok(SubscriptionResourceAssembler.toResource(
                queryService.handle(new GetSubscriptionQuery(principal.tenantId()))));
    }
}