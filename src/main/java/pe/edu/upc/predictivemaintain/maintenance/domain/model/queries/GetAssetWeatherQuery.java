package pe.edu.upc.predictivemaintain.maintenance.domain.model.queries;

import java.util.UUID;

public record GetAssetWeatherQuery(UUID tenantId, UUID assetId) {
}