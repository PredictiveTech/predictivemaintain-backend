package pe.edu.upc.predictivemaintain.subscription.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upc.predictivemaintain.subscription.application.queryservices.SubscriptionPlanQueryService;
import pe.edu.upc.predictivemaintain.subscription.domain.model.queries.GetAllSubscriptionPlansQuery;
import pe.edu.upc.predictivemaintain.subscription.interfaces.rest.resources.SubscriptionPlanResource;
import pe.edu.upc.predictivemaintain.subscription.interfaces.rest.transform.SubscriptionPlanResourceFromEntityAssembler;

import java.util.List;

@RestController
@RequestMapping(value = "/api/plans", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Subscription plans", description = "Public catalog of subscription plans")
public class SubscriptionPlansController {

    private final SubscriptionPlanQueryService planQueryService;

    public SubscriptionPlansController(SubscriptionPlanQueryService planQueryService) {
        this.planQueryService = planQueryService;
    }

    @GetMapping
    @Operation(summary = "List subscription plans",
            description = "Public endpoint used by the Landing Page to show plans and prices (US-25).")
    public ResponseEntity<List<SubscriptionPlanResource>> getAllPlans() {
        List<SubscriptionPlanResource> plans = planQueryService.handle(new GetAllSubscriptionPlansQuery()).stream()
                .map(SubscriptionPlanResourceFromEntityAssembler::toResource)
                .toList();
        return ResponseEntity.ok(plans);
    }
}