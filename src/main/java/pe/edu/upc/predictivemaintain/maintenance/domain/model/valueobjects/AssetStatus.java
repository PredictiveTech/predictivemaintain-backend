package pe.edu.upc.predictivemaintain.maintenance.domain.model.valueobjects;

/**
 * The state of an asset as shown in the list. It is computed on every query, never stored:
 * INACTIVE if it was deactivated; IN_ALERT if it has open alerts (in review or confirmed);
 * NO_COMMUNICATION if none of its sensors is transmitting (or it has none); OPERATIONAL otherwise.
 */
public enum AssetStatus {
    OPERATIONAL,
    IN_ALERT,
    NO_COMMUNICATION,
    INACTIVE
}