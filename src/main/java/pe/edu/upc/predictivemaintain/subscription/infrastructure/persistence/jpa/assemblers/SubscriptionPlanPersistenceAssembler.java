package pe.edu.upc.predictivemaintain.subscription.infrastructure.persistence.jpa.assemblers;

import pe.edu.upc.predictivemaintain.subscription.domain.model.aggregates.SubscriptionPlan;
import pe.edu.upc.predictivemaintain.subscription.domain.model.valueobjects.Money;
import pe.edu.upc.predictivemaintain.subscription.infrastructure.persistence.jpa.entities.SubscriptionPlanPersistenceEntity;

/**
 * Translates between the domain aggregate and its JPA entity (anti-corruption layer).
 */
public final class SubscriptionPlanPersistenceAssembler {

    private SubscriptionPlanPersistenceAssembler() {
    }

    public static SubscriptionPlan toDomain(SubscriptionPlanPersistenceEntity entity) {
        return SubscriptionPlan.restore(
                entity.getId(),
                entity.getName(),
                entity.getAssetLimit(),
                new Money(entity.getAmount(), entity.getCurrency()));
    }

    /** Copies the aggregate state into an entity (new or already loaded). */
    public static void copyToEntity(SubscriptionPlan plan, SubscriptionPlanPersistenceEntity entity) {
        entity.setId(plan.getId());
        entity.setName(plan.getName());
        entity.setAssetLimit(plan.getAssetLimit());
        entity.setAmount(plan.getPrice().amount());
        entity.setCurrency(plan.getPrice().currency());
    }
}