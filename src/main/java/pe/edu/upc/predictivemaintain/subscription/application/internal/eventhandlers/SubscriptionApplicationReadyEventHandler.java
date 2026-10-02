package pe.edu.upc.predictivemaintain.subscription.application.internal.eventhandlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import pe.edu.upc.predictivemaintain.subscription.application.commandservices.SubscriptionPlanCommandService;
import pe.edu.upc.predictivemaintain.subscription.domain.model.commands.CreateSubscriptionPlanCommand;
import pe.edu.upc.predictivemaintain.subscription.domain.repositories.SubscriptionPlanRepository;

import java.math.BigDecimal;

/**
 * Seeds the three commercial plans when the application is ready.
 * Prices and limits are PLACEHOLDERS: align them with the pricing shown in the Landing Page.
 */
@Service
public class SubscriptionApplicationReadyEventHandler {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionApplicationReadyEventHandler.class);

    private final SubscriptionPlanCommandService commandService;
    private final SubscriptionPlanRepository planRepository;

    public SubscriptionApplicationReadyEventHandler(SubscriptionPlanCommandService commandService,
                                                    SubscriptionPlanRepository planRepository) {
        this.commandService = commandService;
        this.planRepository = planRepository;
    }

    @EventListener
    public void on(ApplicationReadyEvent event) {
        log.info("Starting subscription plans seeding verification");
        seedIfNotExists("Basic", 10, "49.00");
        seedIfNotExists("Pro", 50, "149.00");
        seedIfNotExists("Enterprise", 500, "499.00");
        log.info("Subscription plans seeding verification finished");
    }

    private void seedIfNotExists(String name, int assetLimit, String amount) {
        if (planRepository.existsByName(name)) {
            return;
        }
        commandService.handle(new CreateSubscriptionPlanCommand(name, assetLimit, new BigDecimal(amount), "USD"));
    }
}