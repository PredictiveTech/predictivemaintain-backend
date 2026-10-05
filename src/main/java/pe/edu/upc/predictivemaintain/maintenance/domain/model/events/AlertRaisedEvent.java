package pe.edu.upc.predictivemaintain.maintenance.domain.model.events;

import pe.edu.upc.predictivemaintain.shared.domain.model.events.DomainEvent;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * An alert was created. Other contexts (notifications) react to it; Maintenance does not know who listens.
 * metric, unit and observedValue are null for alerts that did not come from a sensor reading.
 */
public record AlertRaisedEvent(UUID alertId, UUID tenantId, UUID assetId, String severity,
                               String metric, String unit, BigDecimal observedValue) implements DomainEvent {
}