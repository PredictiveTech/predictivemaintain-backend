package pe.edu.upc.predictivemaintain.maintenance.application.commandservices;

import pe.edu.upc.predictivemaintain.maintenance.domain.model.valueobjects.SyncOutcome;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.valueobjects.WorkOrderStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * The answer to a synchronization batch.
 *
 * @param clockOffsetSeconds how many seconds the device clock is behind the server's (negative: ahead)
 */
public record SyncResult(Instant serverTime, long clockOffsetSeconds, List<Item> items) {

    /**
     * @param messageKey          i18n key explaining the outcome; the interface layer turns it into text
     * @param workOrderStatus     current state on the server (null if the order is unknown or not the technician's)
     * @param workOrderVersion    current version on the server (same condition)
     */
    public record Item(UUID operationId, SyncOutcome outcome, String messageKey, Object[] messageArgs,
                       WorkOrderStatus workOrderStatus, Long workOrderVersion) {
    }
}