package pe.edu.upc.predictivemaintain.maintenance.domain.repositories;

import pe.edu.upc.predictivemaintain.maintenance.domain.model.aggregates.ProductionWindow;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ProductionWindowRepository {

    ProductionWindow save(ProductionWindow window);

    /** Windows that start inside [from, to): each window belongs to the period in which it starts. */
    List<ProductionWindow> findStartingBetween(UUID tenantId, Collection<UUID> assetIds, Instant from, Instant to);
}