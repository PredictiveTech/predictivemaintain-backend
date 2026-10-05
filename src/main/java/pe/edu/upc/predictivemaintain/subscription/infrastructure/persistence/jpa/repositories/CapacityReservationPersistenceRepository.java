package pe.edu.upc.predictivemaintain.subscription.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.predictivemaintain.subscription.domain.model.valueobjects.CapacityReservationStatus;
import pe.edu.upc.predictivemaintain.subscription.infrastructure.persistence.jpa.entities.CapacityReservationPersistenceEntity;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface CapacityReservationPersistenceRepository
        extends JpaRepository<CapacityReservationPersistenceEntity, UUID> {

    Optional<CapacityReservationPersistenceEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    Optional<CapacityReservationPersistenceEntity> findByTenantIdAndOperationId(UUID tenantId, UUID operationId);

    long countBySubscriptionIdAndStatusIn(UUID subscriptionId, Collection<CapacityReservationStatus> statuses);
}