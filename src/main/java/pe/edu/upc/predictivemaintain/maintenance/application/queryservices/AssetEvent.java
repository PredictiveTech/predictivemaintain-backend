package pe.edu.upc.predictivemaintain.maintenance.application.queryservices;

import java.time.Instant;
import java.util.UUID;

/**
 * One entry in the history of an asset: an alert that was raised or a period in which it was stopped.
 *
 * @param endedAt  end of a DOWNTIME; null for an ALERT
 * @param severity severity of an ALERT; null for a DOWNTIME
 * @param status   status of an ALERT; null for a DOWNTIME
 */
public record AssetEvent(EventType type, UUID assetId, UUID referenceId, Instant occurredAt, Instant endedAt,
                         String severity, String status) {

    public enum EventType {
        ALERT,
        DOWNTIME
    }
}