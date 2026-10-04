package pe.edu.upc.predictivemaintain.maintenance.interfaces.acl;

import org.springframework.stereotype.Service;
import pe.edu.upc.predictivemaintain.maintenance.domain.repositories.AssetRepository;

import java.util.UUID;

/**
 * Anti-Corruption Layer: what the Maintenance context exposes to other bounded contexts.
 */
@Service
public class MaintenanceContextFacade {

    /** NOT_FOUND also covers assets of another company. */
    public enum AssetState {
        ACTIVE,
        INACTIVE,
        NOT_FOUND
    }

    private final AssetRepository assetRepository;

    public MaintenanceContextFacade(AssetRepository assetRepository) {
        this.assetRepository = assetRepository;
    }

    public AssetState assetState(UUID tenantId, UUID assetId) {
        return assetRepository.findByIdAndTenantId(assetId, tenantId)
                .map(asset -> asset.isActive() ? AssetState.ACTIVE : AssetState.INACTIVE)
                .orElse(AssetState.NOT_FOUND);
    }
}