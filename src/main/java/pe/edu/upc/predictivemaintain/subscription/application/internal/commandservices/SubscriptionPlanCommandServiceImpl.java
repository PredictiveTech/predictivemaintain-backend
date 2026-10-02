package pe.edu.upc.predictivemaintain.subscription.application.internal.commandservices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.predictivemaintain.shared.application.errors.ApplicationException;
import pe.edu.upc.predictivemaintain.subscription.application.commandservices.SubscriptionPlanCommandService;
import pe.edu.upc.predictivemaintain.subscription.application.errors.SubscriptionError;
import pe.edu.upc.predictivemaintain.subscription.domain.model.aggregates.SubscriptionPlan;
import pe.edu.upc.predictivemaintain.subscription.domain.model.commands.CreateSubscriptionPlanCommand;
import pe.edu.upc.predictivemaintain.subscription.domain.model.valueobjects.Money;
import pe.edu.upc.predictivemaintain.subscription.domain.repositories.SubscriptionPlanRepository;

import java.util.UUID;

@Service
public class SubscriptionPlanCommandServiceImpl implements SubscriptionPlanCommandService {

    private final SubscriptionPlanRepository planRepository;

    public SubscriptionPlanCommandServiceImpl(SubscriptionPlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    @Override
    @Transactional
    public UUID handle(CreateSubscriptionPlanCommand command) {
        if (planRepository.existsByName(command.name())) {
            throw new ApplicationException(SubscriptionError.PLAN_ALREADY_EXISTS, command.name());
        }
        SubscriptionPlan plan = SubscriptionPlan.create(
                command.name(),
                command.assetLimit(),
                new Money(command.amount(), command.currency()));
        return planRepository.save(plan).getId();
    }
}