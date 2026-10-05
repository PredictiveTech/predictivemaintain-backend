package pe.edu.upc.predictivemaintain.maintenance.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.valueobjects.SyncOutcome;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.valueobjects.WorkOrderStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * @param results oldest action first; match each one with the app's queue by operationId
 */
public record SyncResponseResource(Instant serverTime, long clockOffsetSeconds, List<Item> results) {

    /**
     * @param message           explanation in the language of Accept-Language; show it to the technician when the
     *                          outcome is SUPERSEDED or REJECTED
     * @param workOrderStatus   the order's current state on the server, to refresh the local copy (null if unknown)
     * @param workOrderVersion  its current version
     */
    public record Item(UUID operationId, SyncOutcome outcome, String message,
                       @Schema(nullable = true) WorkOrderStatus workOrderStatus,
                       @Schema(nullable = true) Long workOrderVersion) {
    }
}