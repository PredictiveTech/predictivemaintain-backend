package pe.edu.upc.predictivemaintain.maintenance.domain.model.commands;

import java.util.UUID;

public record DeactivateAssetCommand(UUID tenantId, UUID assetId) {
}