package pe.edu.upc.predictivemaintain.notifications.domain.model.queries;

import pe.edu.upc.predictivemaintain.shared.domain.model.valueobjects.PageQuery;

import java.util.UUID;

public record GetMyNotificationsQuery(UUID tenantId, UUID userId, PageQuery page) {
}