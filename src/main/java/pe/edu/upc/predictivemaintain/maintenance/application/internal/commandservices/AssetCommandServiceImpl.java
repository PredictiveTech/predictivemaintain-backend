package pe.edu.upc.predictivemaintain.maintenance.application.internal.commandservices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.predictivemaintain.maintenance.application.commandservices.AssetCommandService;
import pe.edu.upc.predictivemaintain.maintenance.application.errors.MaintenanceError;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.aggregates.Asset;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.commands.DeactivateAssetCommand;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.commands.RegisterAssetCommand;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.commands.UpdateAssetCommand;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.valueobjects.GeoLocation;
import pe.edu.upc.predictivemaintain.maintenance.domain.repositories.AssetRepository;
import pe.edu.upc.predictivemaintain.shared.application.errors.ApplicationException;
import pe.edu.upc.predictivemaintain.subscription.interfaces.acl.SubscriptionContextFacade;

import java.util.UUID;

@Service
public class AssetCommandServiceImpl implements AssetCommandService {

    private final AssetRepository assetRepository;
    private final SubscriptionContextFacade subscriptionContextFacade;

    public AssetCommandServiceImpl(AssetRepository assetRepository,
                                   SubscriptionContextFacade subscriptionContextFacade) {
        this.assetRepository = assetRepository;
        this.subscriptionContextFacade = subscriptionContextFacade;
    }

    @Override
    @Transactional
    public Asset handle(RegisterAssetCommand command) {
        String code = Asset.normalizeCode(command.code());
        if (assetRepository.existsByTenantIdAndCode(command.tenantId(), code)) {
            throw new ApplicationException(MaintenanceError.ASSET_CODE_ALREADY_EXISTS, code);
        }

        UUID assetId = UUID.randomUUID();
        // Same transaction as the asset: if saving it fails, the reservation is rolled back too.
        UUID reservationId = subscriptionContextFacade.reserveAssetCapacity(command.tenantId(), assetId);

        Asset asset = Asset.register(assetId, command.tenantId(), code, command.name(), command.location(),
                command.plantId(), command.productionLine(), command.assetType(), command.criticality(),
                GeoLocation.fromNullable(command.latitude(), command.longitude()), reservationId);
        return assetRepository.save(asset);
    }

    @Override
    @Transactional
    public Asset handle(UpdateAssetCommand command) {
        Asset asset = findAsset(command.tenantId(), command.assetId());
        if (!asset.isActive()) {
            throw new ApplicationException(MaintenanceError.ASSET_INACTIVE);
        }
        asset.update(command.name(), command.location(), command.plantId(), command.productionLine(),
                command.assetType(), command.criticality(),
                GeoLocation.fromNullable(command.latitude(), command.longitude()));
        return assetRepository.save(asset);
    }

    @Override
    @Transactional
    public Asset handle(DeactivateAssetCommand command) {
        Asset asset = findAsset(command.tenantId(), command.assetId());
        if (!asset.isActive()) {
            return asset; // already deactivated: repeating the request changes nothing
        }
        asset.deactivate();
        Asset saved = assetRepository.save(asset);
        subscriptionContextFacade.releaseAssetCapacity(command.tenantId(), asset.getCapacityReservationId());
        return saved;
    }

    private Asset findAsset(UUID tenantId, UUID assetId) {
        return assetRepository.findByIdAndTenantId(assetId, tenantId)
                .orElseThrow(() -> new ApplicationException(MaintenanceError.ASSET_NOT_FOUND));
    }
}