package pe.edu.upc.predictivemaintain.maintenance.domain.model.queries;

import java.util.UUID;

public record GetEvidenceContentQuery(UUID tenantId, UUID restrictToUserId, UUID workOrderId, UUID evidenceId) {
}