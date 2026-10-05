package pe.edu.upc.predictivemaintain.subscription.infrastructure.persistence.jpa.assemblers;

import pe.edu.upc.predictivemaintain.subscription.domain.model.aggregates.CapacityReservation;
import pe.edu.upc.predictivemaintain.subscription.infrastructure.persistence.jpa.entities.CapacityReservationPersistenceEntity;

public final class CapacityReservationPersistenceAssembler {

    private CapacityReservationPersistenceAssembler() {
    }

    public static CapacityReservation toDomain(CapacityReservationPersistenceEntity entity) {
        return CapacityReservation.restore(entity.getId(), entity.getTenantId(), entity.getSubscriptionId(),
                entity.getOperationId(), entity.getAssetId(), entity.getStatus(), entity.getReservedAt());
    }

    public static void copyToEntity(CapacityReservation reservation, CapacityReservationPersistenceEntity entity) {
        entity.setId(reservation.getId());
        entity.setTenantId(reservation.getTenantId());
        entity.setSubscriptionId(reservation.getSubscriptionId());
        entity.setOperationId(reservation.getOperationId());
        entity.setAssetId(reservation.getAssetId());
        entity.setStatus(reservation.getStatus());
        entity.setReservedAt(reservation.getCreatedAt());
    }
}