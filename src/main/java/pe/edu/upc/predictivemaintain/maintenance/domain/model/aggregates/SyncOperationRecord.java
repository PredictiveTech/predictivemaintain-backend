package pe.edu.upc.predictivemaintain.maintenance.domain.model.aggregates;

import pe.edu.upc.predictivemaintain.maintenance.domain.model.valueobjects.SyncOperationType;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.valueobjects.SyncOutcome;

import java.time.Instant;
import java.util.UUID;

/**
 * Memory of an offline action already processed. It is what makes the synchronization idempotent: the app can
 * resend the same batch as many times as needed (a lost answer, a retry) and nothing is applied twice.
 *
 * @param actionTime when the technician acted, already translated to server time
 */
public record SyncOperationRecord(UUID id, UUID tenantId, UUID operationId, UUID workOrderId,
                                  SyncOperationType type, SyncOutcome outcome,
                                  Instant actionTime, Instant processedAt) {

    public static SyncOperationRecord create(UUID tenantId, UUID operationId, UUID workOrderId,
                                             SyncOperationType type, SyncOutcome outcome,
                                             Instant actionTime, Instant processedAt) {
        return new SyncOperationRecord(UUID.randomUUID(), tenantId, operationId, workOrderId, type, outcome,
                actionTime, processedAt);
    }
}