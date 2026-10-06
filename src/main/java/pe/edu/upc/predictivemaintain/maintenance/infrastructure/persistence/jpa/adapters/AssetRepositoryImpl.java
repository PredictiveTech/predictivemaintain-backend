package pe.edu.upc.predictivemaintain.maintenance.infrastructure.persistence.jpa.adapters;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.aggregates.Asset;
import pe.edu.upc.predictivemaintain.maintenance.domain.repositories.AssetRepository;
import pe.edu.upc.predictivemaintain.maintenance.infrastructure.persistence.jpa.assemblers.AssetPersistenceAssembler;
import pe.edu.upc.predictivemaintain.maintenance.infrastructure.persistence.jpa.entities.AssetPersistenceEntity;
import pe.edu.upc.predictivemaintain.maintenance.infrastructure.persistence.jpa.repositories.AssetPersistenceRepository;
import pe.edu.upc.predictivemaintain.shared.domain.model.valueobjects.PageQuery;
import pe.edu.upc.predictivemaintain.shared.domain.model.valueobjects.PagedResult;
import pe.edu.upc.predictivemaintain.shared.domain.services.DomainEventPublisher;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Collection;

@Repository
public class AssetRepositoryImpl implements AssetRepository {

    private final AssetPersistenceRepository jpaRepository;
    private final DomainEventPublisher eventPublisher;

    public AssetRepositoryImpl(AssetPersistenceRepository jpaRepository, DomainEventPublisher eventPublisher) {
        this.jpaRepository = jpaRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Asset save(Asset asset) {
        AssetPersistenceEntity entity = jpaRepository.findById(asset.getId())
                .orElseGet(AssetPersistenceEntity::new);
        AssetPersistenceAssembler.copyToEntity(asset, entity);
        AssetPersistenceEntity saved = jpaRepository.save(entity);
        eventPublisher.publishAll(asset);
        return AssetPersistenceAssembler.toDomain(saved);
    }

    @Override
    public Optional<Asset> findByIdAndTenantId(UUID id, UUID tenantId) {
        return jpaRepository.findByIdAndTenantId(id, tenantId).map(AssetPersistenceAssembler::toDomain);
    }

    @Override
    public List<Asset> findAllByIdsAndTenantId(Collection<UUID> ids, UUID tenantId) {
        return jpaRepository.findByTenantIdAndIdIn(tenantId, ids).stream()
                .map(AssetPersistenceAssembler::toDomain)
                .toList();
    }

    @Override
    public boolean existsByTenantIdAndCode(UUID tenantId, String code) {
        return jpaRepository.existsByTenantIdAndCode(tenantId, code);
    }

    @Override
    public PagedResult<Asset> search(UUID tenantId, String productionLine, String assetType,
                                     boolean includeInactive, PageQuery pageQuery) {
        Specification<AssetPersistenceEntity> specification = (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(builder.equal(root.get("tenantId"), tenantId));
            if (!includeInactive) {
                predicates.add(builder.isTrue(root.get("active")));
            }
            if (productionLine != null) {
                predicates.add(builder.equal(root.get("productionLine"), productionLine));
            }
            if (assetType != null) {
                predicates.add(builder.equal(root.get("assetType"), assetType));
            }
            return builder.and(predicates.toArray(new Predicate[0]));
        };

        Page<AssetPersistenceEntity> page = jpaRepository.findAll(specification,
                PageRequest.of(pageQuery.page(), pageQuery.size(), Sort.by("code")));
        return new PagedResult<>(
                page.getContent().stream().map(AssetPersistenceAssembler::toDomain).toList(),
                page.getTotalElements(),
                pageQuery.page(),
                pageQuery.size());
    }
}