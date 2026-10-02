package pe.edu.upc.predictivemaintain.subscription.infrastructure.persistence.jpa.assemblers;

import pe.edu.upc.predictivemaintain.subscription.domain.model.aggregates.Subscription;
import pe.edu.upc.predictivemaintain.subscription.infrastructure.persistence.jpa.entities.SubscriptionPersistenceEntity;

public final class SubscriptionPersistenceAssembler {

    private SubscriptionPersistenceAssembler() {
    }

    public static Subscription toDomain(SubscriptionPersistenceEntity entity) {
        return Subscription.restore(entity.getId(), entity.getTenantId(), entity.getPlanId(),
                entity.getAssetLimit(), entity.getStatus(), entity.getStartsAt(), entity.getEndsAt());
    }

    public static void copyToEntity(Subscription subscription, SubscriptionPersistenceEntity entity) {
        entity.setId(subscription.getId());
        entity.setTenantId(subscription.getTenantId());
        entity.setPlanId(subscription.getPlanId());
        entity.setAssetLimit(subscription.getAssetLimit());
        entity.setStatus(subscription.getStatus());
        entity.setStartsAt(subscription.getStartsAt());
        entity.setEndsAt(subscription.getEndsAt());
    }
}