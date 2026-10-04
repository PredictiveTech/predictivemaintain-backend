package pe.edu.upc.predictivemaintain.maintenance.domain.model.valueobjects;

/**
 * How much the plant depends on an asset. The AV1 only says "criticidad": these four levels are a
 * decision of this guide.
 */
public enum Criticality {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}