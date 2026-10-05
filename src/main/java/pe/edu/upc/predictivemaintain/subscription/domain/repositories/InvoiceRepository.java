package pe.edu.upc.predictivemaintain.subscription.domain.repositories;

import pe.edu.upc.predictivemaintain.shared.domain.model.valueobjects.PageQuery;
import pe.edu.upc.predictivemaintain.shared.domain.model.valueobjects.PagedResult;
import pe.edu.upc.predictivemaintain.subscription.domain.model.aggregates.Invoice;

import java.util.UUID;

public interface InvoiceRepository {

    Invoice save(Invoice invoice);

    /** Invoices of one company, newest first. */
    PagedResult<Invoice> findByTenantId(UUID tenantId, PageQuery page);
}