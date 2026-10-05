package pe.edu.upc.predictivemaintain.maintenance.domain.model.commands;

import pe.edu.upc.predictivemaintain.maintenance.domain.model.valueobjects.SyncOperationType;

import java.time.Instant;
import java.util.UUID;

/**
 * One action done offline.
 *
 * @param operationId  chosen by the app; sending the same one twice never applies the action twice
 * @param performedAt  when the technician did it, by the DEVICE's clock
 * @param summary      the corrective actions, required for COMPLETE
 */
public record SyncOperation(UUID operationId, UUID workOrderId, SyncOperationType type,
                            Instant performedAt, String summary) {
}