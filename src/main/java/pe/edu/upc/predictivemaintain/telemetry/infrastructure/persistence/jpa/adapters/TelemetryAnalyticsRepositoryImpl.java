package pe.edu.upc.predictivemaintain.telemetry.infrastructure.persistence.jpa.adapters;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import pe.edu.upc.predictivemaintain.shared.domain.model.valueobjects.PageQuery;
import pe.edu.upc.predictivemaintain.shared.domain.model.valueobjects.PagedResult;
import pe.edu.upc.predictivemaintain.telemetry.domain.model.valueobjects.DetectionView;
import pe.edu.upc.predictivemaintain.telemetry.domain.repositories.TelemetryAnalyticsRepository;
import pe.edu.upc.predictivemaintain.telemetry.infrastructure.persistence.jpa.assemblers.AnomalyDetectionPersistenceAssembler;
import pe.edu.upc.predictivemaintain.telemetry.infrastructure.persistence.jpa.assemblers.SensorReadingPersistenceAssembler;
import pe.edu.upc.predictivemaintain.telemetry.infrastructure.persistence.jpa.entities.AnomalyDetectionPersistenceEntity;
import pe.edu.upc.predictivemaintain.telemetry.infrastructure.persistence.jpa.entities.SensorReadingPersistenceEntity;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Read-only JPQL queries that join detections with the readings that originated them.
 */
@Repository
public class TelemetryAnalyticsRepositoryImpl implements TelemetryAnalyticsRepository {

    private static final String FROM_AND_WHERE =
            "from AnomalyDetectionPersistenceEntity d, SensorReadingPersistenceEntity r "
                    + "where d.readingId = r.id and d.tenantId = :tenantId and r.sensorId in :sensorIds "
                    + "and d.detectedAt >= :from and d.detectedAt <= :to";

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public PagedResult<DetectionView> findDetections(UUID tenantId, Collection<UUID> sensorIds, Instant from,
                                                     Instant to, PageQuery page) {
        if (sensorIds.isEmpty()) {
            return new PagedResult<>(List.of(), 0, page.page(), page.size());
        }

        Long total = bind(entityManager.createQuery("select count(d) " + FROM_AND_WHERE, Long.class),
                tenantId, sensorIds, from, to).getSingleResult();

        List<Object[]> rows = bind(entityManager.createQuery(
                        "select d, r " + FROM_AND_WHERE + " order by d.detectedAt desc", Object[].class),
                tenantId, sensorIds, from, to)
                .setFirstResult(page.page() * page.size())
                .setMaxResults(page.size())
                .getResultList();

        List<DetectionView> views = rows.stream()
                .map(row -> new DetectionView(
                        AnomalyDetectionPersistenceAssembler.toDomain((AnomalyDetectionPersistenceEntity) row[0]),
                        SensorReadingPersistenceAssembler.toDomain((SensorReadingPersistenceEntity) row[1])))
                .toList();
        return new PagedResult<>(views, total, page.page(), page.size());
    }

    private static <T> TypedQuery<T> bind(TypedQuery<T> query, UUID tenantId, Collection<UUID> sensorIds,
                                          Instant from, Instant to) {
        return query.setParameter("tenantId", tenantId)
                .setParameter("sensorIds", sensorIds)
                .setParameter("from", from)
                .setParameter("to", to);
    }
}