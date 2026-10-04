package pe.edu.upc.predictivemaintain.maintenance.domain.model.valueobjects;

/**
 * OPEN -> ASSIGNED -> IN_PROGRESS -> COMPLETED. It can be CANCELLED only from OPEN or ASSIGNED.
 * A completed order is never reopened.
 */
public enum WorkOrderStatus {
    OPEN,
    ASSIGNED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}