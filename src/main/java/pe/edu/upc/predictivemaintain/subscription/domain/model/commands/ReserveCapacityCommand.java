package pe.edu.upc.predictivemaintain.subscription.domain.model.commands;

import java.util.UUID;

/**
 * @param operationId idempotency key: repeating it returns the same reservation instead of consuming another slot
 */
public record ReserveCapacityCommand(UUID tenantId, UUID operationId, UUID assetId) {
}