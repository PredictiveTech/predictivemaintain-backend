package pe.edu.upc.predictivemaintain.iam.domain.model.queries;

import pe.edu.upc.predictivemaintain.iam.domain.model.valueobjects.RoleName;
import pe.edu.upc.predictivemaintain.shared.domain.model.valueobjects.PageQuery;

import java.util.UUID;

/**
 * The users of one company.
 *
 * @param role   only the users that have this role; null means any
 * @param active true for active users only, false for deactivated ones only; null means both
 */
public record GetUsersQuery(UUID tenantId, RoleName role, Boolean active, PageQuery page) {
}