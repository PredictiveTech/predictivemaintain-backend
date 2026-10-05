package pe.edu.upc.predictivemaintain.subscription.domain.repositories;

import pe.edu.upc.predictivemaintain.subscription.domain.model.aggregates.Subscription;

import java.util.Optional;
import java.util.UUID;
import java.time.Instant;
import java.util.List;

public interface SubscriptionRepository {

    Subscription save(Subscription subscription);

    /** Most recent subscription of the company. */
    Optional<Subscription> findCurrentByTenantId(UUID tenantId);

    /**
     * Same as {@link #findCurrentByTenantId} but locks the row until the transaction ends,
     * so two reservations for the same company are processed one at a time.
     */
    Optional<Subscription> findCurrentForUpdate(UUID tenantId);

    /** ACTIVE subscriptions whose end falls after "from" and not after "to". */
    List<Subscription> findActiveEndingBetween(Instant from, Instant to);
}