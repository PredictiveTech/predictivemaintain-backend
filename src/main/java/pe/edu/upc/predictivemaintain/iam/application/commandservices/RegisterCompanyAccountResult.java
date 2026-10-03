package pe.edu.upc.predictivemaintain.iam.application.commandservices;

import java.util.UUID;

/**
 * @param created false when the request was a repeat of an earlier registration (idempotent replay)
 */
public record RegisterCompanyAccountResult(UUID tenantId, UUID userId, boolean created) {
}