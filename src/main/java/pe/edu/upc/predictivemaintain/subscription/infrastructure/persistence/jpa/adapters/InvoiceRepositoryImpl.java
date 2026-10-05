package pe.edu.upc.predictivemaintain.subscription.infrastructure.persistence.jpa.adapters;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import pe.edu.upc.predictivemaintain.shared.domain.model.valueobjects.PageQuery;
import pe.edu.upc.predictivemaintain.shared.domain.model.valueobjects.PagedResult;
import pe.edu.upc.predictivemaintain.shared.domain.services.DomainEventPublisher;
import pe.edu.upc.predictivemaintain.subscription.domain.model.aggregates.Invoice;
import pe.edu.upc.predictivemaintain.subscription.domain.repositories.InvoiceRepository;
import pe.edu.upc.predictivemaintain.subscription.infrastructure.persistence.jpa.assemblers.InvoicePersistenceAssembler;
import pe.edu.upc.predictivemaintain.subscription.infrastructure.persistence.jpa.entities.InvoicePersistenceEntity;
import pe.edu.upc.predictivemaintain.subscription.infrastructure.persistence.jpa.repositories.InvoicePersistenceRepository;

import java.util.UUID;

@Repository
public class InvoiceRepositoryImpl implements InvoiceRepository {

    private final InvoicePersistenceRepository jpaRepository;
    private final DomainEventPublisher eventPublisher;

    public InvoiceRepositoryImpl(InvoicePersistenceRepository jpaRepository, DomainEventPublisher eventPublisher) {
        this.jpaRepository = jpaRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Invoice save(Invoice invoice) {
        InvoicePersistenceEntity entity = jpaRepository.findById(invoice.getId())
                .orElseGet(InvoicePersistenceEntity::new);
        InvoicePersistenceAssembler.copyToEntity(invoice, entity);
        InvoicePersistenceEntity saved = jpaRepository.save(entity);
        eventPublisher.publishAll(invoice);
        return InvoicePersistenceAssembler.toDomain(saved);
    }

    @Override
    public PagedResult<Invoice> findByTenantId(UUID tenantId, PageQuery pageQuery) {
        Page<InvoicePersistenceEntity> page = jpaRepository.findByTenantId(tenantId,
                PageRequest.of(pageQuery.page(), pageQuery.size(), Sort.by(Sort.Direction.DESC, "issuedAt")));
        return new PagedResult<>(
                page.getContent().stream().map(InvoicePersistenceAssembler::toDomain).toList(),
                page.getTotalElements(),
                pageQuery.page(),
                pageQuery.size());
    }
}