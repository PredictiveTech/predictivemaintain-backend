package pe.edu.upc.predictivemaintain.maintenance.domain.model.queries;

import java.util.Collection;
import java.util.UUID;

/**
 * What each alert in a set is about (its asset and severity), to put a readable label on lists of work orders.
 */
public record GetAlertLabelsQuery(UUID tenantId, Collection<UUID> alertIds) {
}