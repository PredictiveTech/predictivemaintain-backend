package pe.edu.upc.predictivemaintain.maintenance.interfaces.rest.resources;

import java.time.Instant;
import java.util.UUID;

public record EvidenceResource(UUID id, UUID workOrderId, String mimeType, Instant uploadedAt) {
}