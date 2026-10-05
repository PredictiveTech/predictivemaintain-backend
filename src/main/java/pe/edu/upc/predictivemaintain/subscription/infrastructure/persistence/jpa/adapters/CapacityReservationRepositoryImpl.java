package pe.edu.upc.predictivemaintain.subscription.infrastructure.persistence.jpa.adapters;

import org.springframework.stereotype.Repository;
import pe.edu.upc.predictivemaintain.shared.domain.services.DomainEventPublisher;
import pe.edu.upc.predictivemaintain.subscription.domain.model.aggregates.CapacityReservation;
import pe.edu.upc.predictivemaintain.subscription.domain.model.valueobjects.CapacityReservationStatus;
import pe.edu.upc.predictivemaintain.subscription.domain.repositories.CapacityReservationRepository;
import pe.edu.upc.predictivemaintain.subscription.infrastructure.persistence.jpa.assemblers.CapacityReservationPersistenceAssembler;
import pe.edu.upc.predictivemaintain.subscription.infrastructure.persistence.jpa.entities.CapacityReservationPersistenceEntity;
import pe.edu.upc.predictivemaintain.subscription.infrastructure.persistence.jpa.repositories.CapacityReservationPersistenceRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class CapacityReservationRepositoryImpl implements CapacityReservationRepository {

    private final CapacityReservationPersistenceRepository jpaRepository;
    private final DomainEventPublisher eventPublisher;

    public CapacityReservationRepositoryImpl(CapacityReservationPersistenceRepository jpaRepository,
                                             DomainEventPublisher eventPublisher) {
        this.jpaRepository = jpaRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public CapacityReservation save(CapacityReservation reservation) {
        CapacityReservationPersistenceEntity entity = jpaRepository.findById(reservation.getId())
                .orElseGet(CapacityReservationPersistenceEntity::new);
        CapacityReservationPersistenceAssembler.copyToEntity(reservation, entity);
        CapacityReservationPersistenceEntity saved = jpaRepository.save(entity);
        eventPublisher.publishAll(reservation);
        return CapacityReservationPersistenceAssembler.toDomain(saved);
    }

    @Override
    public Optional<CapacityReservation> findByIdAndTenantId(UUID id, UUID tenantId) {
        return jpaRepository.findByIdAndTenantId(id, tenantId)
                .map(CapacityReservationPersistenceAssembler::toDomain);
    }

    @Override
    public Optional<CapacityReservation> findByTenantIdAndOperationId(UUID tenantId, UUID operationId) {
        return jpaRepository.findByTenantIdAndOperationId(tenantId, operationId)
                .map(CapacityReservationPersistenceAssembler::toDomain);
    }

    @Override
    public long countOccupiedBySubscriptionId(UUID subscriptionId) {
        return jpaRepository.countBySubscriptionIdAndStatusIn(subscriptionId,
                List.of(CapacityReservationStatus.RESERVED, CapacityReservationStatus.CONFIRMED));
    }
}