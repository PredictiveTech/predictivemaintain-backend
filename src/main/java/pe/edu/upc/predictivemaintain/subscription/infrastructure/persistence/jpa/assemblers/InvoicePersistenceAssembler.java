package pe.edu.upc.predictivemaintain.subscription.infrastructure.persistence.jpa.assemblers;

import pe.edu.upc.predictivemaintain.subscription.domain.model.aggregates.Invoice;
import pe.edu.upc.predictivemaintain.subscription.domain.model.valueobjects.Money;
import pe.edu.upc.predictivemaintain.subscription.infrastructure.persistence.jpa.entities.InvoicePersistenceEntity;

public final class InvoicePersistenceAssembler {

    private InvoicePersistenceAssembler() {
    }

    public static Invoice toDomain(InvoicePersistenceEntity entity) {
        return Invoice.restore(entity.getId(), entity.getTenantId(), entity.getSubscriptionId(), entity.getNumber(),
                entity.getConcept(), new Money(entity.getAmount(), entity.getCurrency()), entity.getStatus(),
                entity.getIssuedAt(), entity.getPaymentReference());
    }

    public static void copyToEntity(Invoice invoice, InvoicePersistenceEntity entity) {
        entity.setId(invoice.getId());
        entity.setTenantId(invoice.getTenantId());
        entity.setSubscriptionId(invoice.getSubscriptionId());
        entity.setNumber(invoice.getNumber());
        entity.setConcept(invoice.getConcept());
        entity.setAmount(invoice.getTotal().amount());
        entity.setCurrency(invoice.getTotal().currency());
        entity.setStatus(invoice.getStatus());
        entity.setIssuedAt(invoice.getIssuedAt());
        entity.setPaymentReference(invoice.getPaymentReference());
    }
}