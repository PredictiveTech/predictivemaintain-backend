package pe.edu.upc.predictivemaintain.telemetry.domain.model.queries;

import java.util.UUID;

public record GetAssetSensorsQuery(UUID tenantId, UUID assetId) {
}