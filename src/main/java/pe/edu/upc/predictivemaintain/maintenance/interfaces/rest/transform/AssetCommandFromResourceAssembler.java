package pe.edu.upc.predictivemaintain.maintenance.interfaces.rest.transform;

import pe.edu.upc.predictivemaintain.maintenance.domain.model.commands.RegisterAssetCommand;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.commands.UpdateAssetCommand;
import pe.edu.upc.predictivemaintain.maintenance.interfaces.rest.resources.CreateAssetResource;
import pe.edu.upc.predictivemaintain.maintenance.interfaces.rest.resources.UpdateAssetResource;

import java.util.UUID;

/**
 * The tenantId always comes from the authenticated identity, never from the request body.
 */
public final class AssetCommandFromResourceAssembler {

    private AssetCommandFromResourceAssembler() {
    }

    public static RegisterAssetCommand toCommand(UUID tenantId, CreateAssetResource resource) {
        return new RegisterAssetCommand(tenantId, resource.code(), resource.name(), resource.location(),
                resource.plantId(), resource.productionLine(), resource.assetType(), resource.criticality(),
                resource.latitude(), resource.longitude());
    }

    public static UpdateAssetCommand toCommand(UUID tenantId, UUID assetId, UpdateAssetResource resource) {
        return new UpdateAssetCommand(tenantId, assetId, resource.name(), resource.location(),
                resource.plantId(), resource.productionLine(), resource.assetType(), resource.criticality(),
                resource.latitude(), resource.longitude());
    }
}