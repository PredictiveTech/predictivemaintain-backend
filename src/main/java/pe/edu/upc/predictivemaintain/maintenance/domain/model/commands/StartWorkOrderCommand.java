package pe.edu.upc.predictivemaintain.maintenance.domain.model.commands;

import java.time.Instant;
import java.util.UUID;

/**
 * @param performedAt when the action really happened; null means "now". Offline synchronization sets it so the
 *                    history shows when the technician acted, not when the phone got a connection.
 */
public record StartWorkOrderCommand(UUID tenantId, UUID actorId, UUID workOrderId, Long expectedVersion,
                                    Instant performedAt) {

    /** The usual case: the action happens now. */
    public StartWorkOrderCommand(UUID tenantId, UUID actorId, UUID workOrderId, Long expectedVersion) {
        this(tenantId, actorId, workOrderId, expectedVersion, null);
    }
}