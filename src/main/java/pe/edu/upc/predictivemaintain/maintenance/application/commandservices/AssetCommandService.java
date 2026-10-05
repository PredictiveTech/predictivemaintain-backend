package pe.edu.upc.predictivemaintain.maintenance.application.commandservices;

import pe.edu.upc.predictivemaintain.maintenance.domain.model.aggregates.Asset;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.commands.DeactivateAssetCommand;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.commands.RegisterAssetCommand;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.commands.UpdateAssetCommand;

/**
 * Write use cases for assets.
 */
public interface AssetCommandService {

    Asset handle(RegisterAssetCommand command);

    Asset handle(UpdateAssetCommand command);

    Asset handle(DeactivateAssetCommand command);
}