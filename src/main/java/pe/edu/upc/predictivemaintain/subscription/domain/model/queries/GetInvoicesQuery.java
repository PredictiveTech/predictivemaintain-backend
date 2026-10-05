package pe.edu.upc.predictivemaintain.subscription.domain.model.queries;

import pe.edu.upc.predictivemaintain.shared.domain.model.valueobjects.PageQuery;

import java.util.UUID;

public record GetInvoicesQuery(UUID tenantId, PageQuery page) {
}