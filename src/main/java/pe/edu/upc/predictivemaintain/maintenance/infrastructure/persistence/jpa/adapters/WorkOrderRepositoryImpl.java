package pe.edu.upc.predictivemaintain.maintenance.infrastructure.persistence.jpa.adapters;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.aggregates.WorkOrder;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.aggregates.WorkOrderChange;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.valueobjects.WorkOrderStatus;
import pe.edu.upc.predictivemaintain.maintenance.domain.repositories.WorkOrderRepository;
import pe.edu.upc.predictivemaintain.maintenance.infrastructure.persistence.jpa.assemblers.WorkOrderChangePersistenceAssembler;
import pe.edu.upc.predictivemaintain.maintenance.infrastructure.persistence.jpa.assemblers.WorkOrderPersistenceAssembler;
import pe.edu.upc.predictivemaintain.maintenance.infrastructure.persistence.jpa.entities.WorkOrderChangePersistenceEntity;
import pe.edu.upc.predictivemaintain.maintenance.infrastructure.persistence.jpa.entities.WorkOrderPersistenceEntity;
import pe.edu.upc.predictivemaintain.maintenance.infrastructure.persistence.jpa.repositories.WorkOrderChangePersistenceRepository;
import pe.edu.upc.predictivemaintain.maintenance.infrastructure.persistence.jpa.repositories.WorkOrderPersistenceRepository;
import pe.edu.upc.predictivemaintain.shared.domain.model.valueobjects.PageQuery;
import pe.edu.upc.predictivemaintain.shared.domain.model.valueobjects.PagedResult;
import pe.edu.upc.predictivemaintain.shared.domain.services.DomainEventPublisher;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class WorkOrderRepositoryImpl implements WorkOrderRepository {

    private static final List<WorkOrderStatus> OPEN_STATUSES =
            List.of(WorkOrderStatus.OPEN, WorkOrderStatus.ASSIGNED, WorkOrderStatus.IN_PROGRESS);

    private final WorkOrderPersistenceRepository jpaRepository;
    private final WorkOrderChangePersistenceRepository changeJpaRepository;
    private final DomainEventPublisher eventPublisher;

    public WorkOrderRepositoryImpl(WorkOrderPersistenceRepository jpaRepository,
                                   WorkOrderChangePersistenceRepository changeJpaRepository,
                                   DomainEventPublisher eventPublisher) {
        this.jpaRepository = jpaRepository;
        this.changeJpaRepository = changeJpaRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public WorkOrder save(WorkOrder order) {
        WorkOrderPersistenceEntity entity = jpaRepository.findById(order.getId())
                .orElseGet(WorkOrderPersistenceEntity::new);
        WorkOrderPersistenceAssembler.copyToEntity(order, entity);
        WorkOrderPersistenceEntity saved = jpaRepository.saveAndFlush(entity);

        // The history entries are saved in the same transaction as the order.
        List<WorkOrderChangePersistenceEntity> changes = order.drainChanges().stream()
                .map(WorkOrderChangePersistenceAssembler::toEntity)
                .toList();
        changeJpaRepository.saveAll(changes);

        eventPublisher.publishAll(order);
        return WorkOrderPersistenceAssembler.toDomain(saved);
    }

    @Override
    public Optional<WorkOrder> findByIdAndTenantId(UUID id, UUID tenantId) {
        return jpaRepository.findByIdAndTenantId(id, tenantId).map(WorkOrderPersistenceAssembler::toDomain);
    }

    @Override
    public Optional<WorkOrder> findByAlertIdAndTenantId(UUID alertId, UUID tenantId) {
        return jpaRepository.findByAlertIdAndTenantId(alertId, tenantId)
                .map(WorkOrderPersistenceAssembler::toDomain);
    }

    @Override
    public PagedResult<WorkOrder> search(UUID tenantId, UUID assignedUserId, WorkOrderStatus status,
                                         PageQuery pageQuery) {
        Specification<WorkOrderPersistenceEntity> specification = (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(builder.equal(root.get("tenantId"), tenantId));
            if (assignedUserId != null) {
                predicates.add(builder.equal(root.get("assignedUserId"), assignedUserId));
            }
            if (status != null) {
                predicates.add(builder.equal(root.get("status"), status));
            }
            return builder.and(predicates.toArray(new Predicate[0]));
        };

        Page<WorkOrderPersistenceEntity> page = jpaRepository.findAll(specification,
                PageRequest.of(pageQuery.page(), pageQuery.size(), Sort.by(Sort.Direction.DESC, "openedAt")));
        return new PagedResult<>(
                page.getContent().stream().map(WorkOrderPersistenceAssembler::toDomain).toList(),
                page.getTotalElements(),
                pageQuery.page(),
                pageQuery.size());
    }

    @Override
    public long countOpenByAssetId(UUID tenantId, UUID assetId) {
        return jpaRepository.countByAssetIdAndStatusIn(tenantId, assetId, OPEN_STATUSES);
    }

    @Override
    public List<WorkOrderChange> findHistory(UUID tenantId, UUID workOrderId) {
        return changeJpaRepository.findByTenantIdAndWorkOrderIdOrderByChangedAtAsc(tenantId, workOrderId).stream()
                .map(WorkOrderChangePersistenceAssembler::toDomain)
                .toList();
    }
}