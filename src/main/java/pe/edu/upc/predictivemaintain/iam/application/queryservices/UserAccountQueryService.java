package pe.edu.upc.predictivemaintain.iam.application.queryservices;

import pe.edu.upc.predictivemaintain.iam.domain.model.aggregates.UserAccount;
import pe.edu.upc.predictivemaintain.iam.domain.model.queries.GetUserAccountByIdQuery;
import pe.edu.upc.predictivemaintain.iam.domain.model.queries.GetUsersQuery;
import pe.edu.upc.predictivemaintain.shared.domain.model.valueobjects.PagedResult;

public interface UserAccountQueryService {


    UserAccount handle(GetUserAccountByIdQuery query);

    /** The users of the company, paginated and filtered (maintenance managers only; the controller checks it). */
    PagedResult<UserAccount> handle(GetUsersQuery query);
}