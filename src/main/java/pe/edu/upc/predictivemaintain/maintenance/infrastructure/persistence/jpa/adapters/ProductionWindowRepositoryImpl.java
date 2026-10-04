package pe.edu.upc.predictivemaintain.maintenance.infrastructure.persistence.jpa.adapters;

import org.springframework.stereotype.Repository;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.aggregates.ProductionWindow;
import pe.edu.upc.predictivemaintain.maintenance.domain.repositories.ProductionWindowRepository;
import pe.edu.upc.predictivemaintain.maintenance.infrastructure.persistence.jpa.assemblers.ProductionWindowPersistenceAssembler;
import pe.edu.upc.predictivemaintain.maintenance.infrastructure.persistence.jpa.repositories.ProductionWindowPersistenceRepository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public class ProductionWindowRepositoryImpl implements ProductionWindowRepository {

    private final ProductionWindowPersistenceRepository jpaRepository;

    public ProductionWindowRepositoryImpl(ProductionWindowPersistenceRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public ProductionWindow save(ProductionWindow window) {
        return ProductionWindowPersistenceAssembler.toDomain(
                jpaRepository.save(ProductionWindowPersistenceAssembler.toEntity(window)));
    }

    @Override
    public List<ProductionWindow> findStartingBetween(UUID tenantId, Collection<UUID> assetIds,
                                                      Instant from, Instant to) {
        if (assetIds.isEmpty()) {
            return List.of();
        }
        return jpaRepository
                .findByTenantIdAndAssetIdInAndStartsAtGreaterThanEqualAndStartsAtLessThan(tenantId, assetIds, from, to)
                .stream()
                .map(ProductionWindowPersistenceAssembler::toDomain)
                .toList();
    }
}