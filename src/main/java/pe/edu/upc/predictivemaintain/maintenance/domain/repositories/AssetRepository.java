package pe.edu.upc.predictivemaintain.maintenance.domain.repositories;

import pe.edu.upc.predictivemaintain.maintenance.domain.model.aggregates.Asset;
import pe.edu.upc.predictivemaintain.shared.domain.model.valueobjects.PageQuery;
import pe.edu.upc.predictivemaintain.shared.domain.model.valueobjects.PagedResult;

import java.util.Optional;
import java.util.UUID;
import java.util.Collection;
import java.util.List;

public interface AssetRepository {

    Asset save(Asset asset);

    /** Always filtered by company: an asset of another company looks like it does not exist. */
    Optional<Asset> findByIdAndTenantId(UUID id, UUID tenantId);

    boolean existsByTenantIdAndCode(UUID tenantId, String code);

    /** Several assets of one company in a single query. Ids that do not exist or belong to another company are left out. */
    List<Asset> findAllByIdsAndTenantId(Collection<UUID> ids, UUID tenantId);

    PagedResult<Asset> search(UUID tenantId, String productionLine, String assetType,
                              boolean includeInactive, PageQuery page);
}