package pe.edu.upc.predictivemaintain.maintenance.domain.model.commands;

import java.util.UUID;

public record AttachEvidenceCommand(UUID tenantId, UUID actorId, UUID workOrderId, byte[] content) {
}