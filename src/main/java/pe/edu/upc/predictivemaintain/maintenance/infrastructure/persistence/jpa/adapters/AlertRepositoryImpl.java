package pe.edu.upc.predictivemaintain.maintenance.infrastructure.persistence.jpa.adapters;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.aggregates.Alert;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.valueobjects.AlertSeverity;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.valueobjects.AlertStatus;
import pe.edu.upc.predictivemaintain.maintenance.domain.repositories.AlertRepository;
import pe.edu.upc.predictivemaintain.maintenance.infrastructure.persistence.jpa.assemblers.AlertPersistenceAssembler;
import pe.edu.upc.predictivemaintain.maintenance.infrastructure.persistence.jpa.entities.AlertPersistenceEntity;
import pe.edu.upc.predictivemaintain.maintenance.infrastructure.persistence.jpa.repositories.AlertPersistenceRepository;
import pe.edu.upc.predictivemaintain.shared.domain.model.valueobjects.PageQuery;
import pe.edu.upc.predictivemaintain.shared.domain.model.valueobjects.PagedResult;
import pe.edu.upc.predictivemaintain.shared.domain.services.DomainEventPublisher;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class AlertRepositoryImpl implements AlertRepository {

    private final AlertPersistenceRepository jpaRepository;
    private final DomainEventPublisher eventPublisher;

    public AlertRepositoryImpl(AlertPersistenceRepository jpaRepository, DomainEventPublisher eventPublisher) {
        this.jpaRepository = jpaRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Alert save(Alert alert) {
        AlertPersistenceEntity entity = jpaRepository.findById(alert.getId())
                .orElseGet(AlertPersistenceEntity::new);
        AlertPersistenceAssembler.copyToEntity(alert, entity);
        // saveAndFlush so the new version number is already in the entity we return.
        AlertPersistenceEntity saved = jpaRepository.saveAndFlush(entity);
        eventPublisher.publishAll(alert);
        return AlertPersistenceAssembler.toDomain(saved);
    }

    @Override
    public Optional<Alert> findByIdAndTenantId(UUID id, UUID tenantId) {
        return jpaRepository.findByIdAndTenantId(id, tenantId).map(AlertPersistenceAssembler::toDomain);
    }

    @Override
    public Optional<Alert> findByTenantIdAndSourceEventId(UUID tenantId, UUID sourceEventId) {
        return jpaRepository.findByTenantIdAndSourceEventId(tenantId, sourceEventId)
                .map(AlertPersistenceAssembler::toDomain);
    }

    @Override
    public PagedResult<Alert> search(UUID tenantId, AlertSeverity severity, AlertStatus status,
                                     UUID assetId, PageQuery pageQuery) {
        Specification<AlertPersistenceEntity> specification = (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(builder.equal(root.get("tenantId"), tenantId));
            if (severity != null) {
                predicates.add(builder.equal(root.get("severity"), severity));
            }
            if (status != null) {
                predicates.add(builder.equal(root.get("status"), status));
            }
            if (assetId != null) {
                predicates.add(builder.equal(root.get("assetId"), assetId));
            }
            return builder.and(predicates.toArray(new Predicate[0]));
        };

        Page<AlertPersistenceEntity> page = jpaRepository.findAll(specification,
                PageRequest.of(pageQuery.page(), pageQuery.size(), Sort.by(Sort.Direction.DESC, "raisedAt")));
        return new PagedResult<>(
                page.getContent().stream().map(AlertPersistenceAssembler::toDomain).toList(),
                page.getTotalElements(),
                pageQuery.page(),
                pageQuery.size());
    }
}