package pe.edu.upc.predictivemaintain.subscription.domain.model.valueobjects;

/**
 * State of a capacity reservation. In this monolith reservations are created already CONFIRMED
 * because reserving and creating the asset happen in the same transaction.
 */
public enum CapacityReservationStatus {
    RESERVED,
    CONFIRMED,
    RELEASED
}