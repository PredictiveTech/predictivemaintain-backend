package pe.edu.upc.predictivemaintain.subscription.domain.repositories;

import pe.edu.upc.predictivemaintain.subscription.domain.model.aggregates.CapacityReservation;

import java.util.Optional;
import java.util.UUID;

public interface CapacityReservationRepository {

    CapacityReservation save(CapacityReservation reservation);

    Optional<CapacityReservation> findByIdAndTenantId(UUID id, UUID tenantId);

    Optional<CapacityReservation> findByTenantIdAndOperationId(UUID tenantId, UUID operationId);

    /** Reservations that currently hold a slot (RESERVED or CONFIRMED). */
    long countOccupiedBySubscriptionId(UUID subscriptionId);
}