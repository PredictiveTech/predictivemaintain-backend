package pe.edu.upc.predictivemaintain.maintenance.domain.model.commands;

import java.time.Instant;
import java.util.UUID;

/**
 * @param performedAt when the action really happened; null means "now" (see StartWorkOrderCommand)
 */
public record CompleteWorkOrderCommand(UUID tenantId, UUID actorId, UUID workOrderId, String summary,
                                       Long expectedVersion, Instant performedAt) {

    /** The usual case: the action happens now. */
    public CompleteWorkOrderCommand(UUID tenantId, UUID actorId, UUID workOrderId, String summary,
                                    Long expectedVersion) {
        this(tenantId, actorId, workOrderId, summary, expectedVersion, null);
    }
}