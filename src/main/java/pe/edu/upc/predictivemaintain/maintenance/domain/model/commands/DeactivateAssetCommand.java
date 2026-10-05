package pe.edu.upc.predictivemaintain.maintenance.domain.model.commands;

import java.util.UUID;

/**
 * @param force true confirms the deactivation even if the asset has open work orders
 */
public record DeactivateAssetCommand(UUID tenantId, UUID assetId, boolean force) {

    /** Without confirmation: the deactivation is refused if the asset has open work orders. */
    public DeactivateAssetCommand(UUID tenantId, UUID assetId) {
        this(tenantId, assetId, false);
    }
}