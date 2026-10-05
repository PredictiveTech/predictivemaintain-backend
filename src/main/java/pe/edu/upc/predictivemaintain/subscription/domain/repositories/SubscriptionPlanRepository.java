package pe.edu.upc.predictivemaintain.subscription.domain.repositories;

import pe.edu.upc.predictivemaintain.subscription.domain.model.aggregates.SubscriptionPlan;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistence port of the SubscriptionPlan aggregate. It does not mention JPA.
 */
public interface SubscriptionPlanRepository {

    SubscriptionPlan save(SubscriptionPlan plan);

    Optional<SubscriptionPlan> findByName(String name);

    boolean existsByName(String name);

    List<SubscriptionPlan> findAllOrderedByAssetLimit();

    Optional<SubscriptionPlan> findById(UUID id);
}