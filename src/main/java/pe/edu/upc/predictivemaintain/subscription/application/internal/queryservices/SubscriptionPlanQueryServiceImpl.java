package pe.edu.upc.predictivemaintain.subscription.application.internal.queryservices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.predictivemaintain.subscription.application.queryservices.SubscriptionPlanQueryService;
import pe.edu.upc.predictivemaintain.subscription.domain.model.aggregates.SubscriptionPlan;
import pe.edu.upc.predictivemaintain.subscription.domain.model.queries.GetAllSubscriptionPlansQuery;
import pe.edu.upc.predictivemaintain.subscription.domain.repositories.SubscriptionPlanRepository;

import java.util.List;

@Service
public class SubscriptionPlanQueryServiceImpl implements SubscriptionPlanQueryService {

    private final SubscriptionPlanRepository planRepository;

    public SubscriptionPlanQueryServiceImpl(SubscriptionPlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionPlan> handle(GetAllSubscriptionPlansQuery query) {
        return planRepository.findAllOrderedByAssetLimit();
    }
}