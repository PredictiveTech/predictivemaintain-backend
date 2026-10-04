package pe.edu.upc.predictivemaintain.maintenance.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.predictivemaintain.maintenance.infrastructure.persistence.jpa.entities.ProductionWindowPersistenceEntity;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ProductionWindowPersistenceRepository
        extends JpaRepository<ProductionWindowPersistenceEntity, UUID> {

    List<ProductionWindowPersistenceEntity> findByTenantIdAndAssetIdInAndStartsAtGreaterThanEqualAndStartsAtLessThan(
            UUID tenantId, Collection<UUID> assetIds, Instant from, Instant to);
}