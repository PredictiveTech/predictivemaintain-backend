package pe.edu.upc.predictivemaintain.maintenance.infrastructure.persistence.jpa.adapters;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.aggregates.Alert;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.valueobjects.AlertStatus;
import pe.edu.upc.predictivemaintain.maintenance.domain.repositories.AssetActivityRepository;
import pe.edu.upc.predictivemaintain.maintenance.infrastructure.persistence.jpa.assemblers.AlertPersistenceAssembler;
import pe.edu.upc.predictivemaintain.maintenance.infrastructure.persistence.jpa.entities.AlertPersistenceEntity;

import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Read-only queries written directly in JPQL with the EntityManager. They cross several tables
 * and do not belong to a single aggregate, so they do not need a Spring Data interface.
 */
@Repository
public class AssetActivityRepositoryImpl implements AssetActivityRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Alert> findAlertsRaisedBetween(UUID tenantId, Collection<UUID> assetIds, Instant from, Instant to) {
        if (assetIds.isEmpty()) {
            return List.of();
        }
        return entityManager.createQuery(
                        "select a from AlertPersistenceEntity a "
                                + "where a.tenantId = :tenantId and a.assetId in :assetIds "
                                + "and a.raisedAt >= :from and a.raisedAt <= :to order by a.raisedAt asc",
                        AlertPersistenceEntity.class)
                .setParameter("tenantId", tenantId)
                .setParameter("assetIds", assetIds)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList().stream()
                .map(AlertPersistenceAssembler::toDomain)
                .toList();
    }

    @Override
    public Set<UUID> findAssetIdsWithOpenAlerts(UUID tenantId, Collection<UUID> assetIds) {
        if (assetIds.isEmpty()) {
            return Set.of();
        }
        List<UUID> ids = entityManager.createQuery(
                        "select distinct a.assetId from AlertPersistenceEntity a "
                                + "where a.tenantId = :tenantId and a.assetId in :assetIds and a.status in :statuses",
                        UUID.class)
                .setParameter("tenantId", tenantId)
                .setParameter("assetIds", assetIds)
                .setParameter("statuses", List.of(AlertStatus.IN_REVIEW, AlertStatus.CONFIRMED))
                .getResultList();
        return new HashSet<>(ids);
    }
}