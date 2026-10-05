package pe.edu.upc.predictivemaintain.maintenance.domain.model.valueobjects;

/**
 * IN_REVIEW is the initial state. The manager moves it to CONFIRMED or DISCARDED;
 * completing the work order moves a CONFIRMED alert to RESOLVED.
 */
public enum AlertStatus {
    IN_REVIEW,
    CONFIRMED,
    DISCARDED,
    RESOLVED
}