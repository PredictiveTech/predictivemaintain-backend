package pe.edu.upc.predictivemaintain.maintenance.domain.model.commands;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * @param deviceSentAt what time it was on the device's clock when it sent the batch
 */
public record SyncWorkOrdersCommand(UUID tenantId, UUID actorId, Instant deviceSentAt,
                                    List<SyncOperation> operations) {
}