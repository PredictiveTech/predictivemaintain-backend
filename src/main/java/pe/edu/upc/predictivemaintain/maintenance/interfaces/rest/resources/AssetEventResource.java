package pe.edu.upc.predictivemaintain.maintenance.interfaces.rest.resources;

import pe.edu.upc.predictivemaintain.maintenance.application.queryservices.AssetEvent.EventType;

import java.time.Instant;
import java.util.UUID;

public record AssetEventResource(EventType type, UUID referenceId, Instant occurredAt, Instant endedAt,
                                 String severity, String status) {
}