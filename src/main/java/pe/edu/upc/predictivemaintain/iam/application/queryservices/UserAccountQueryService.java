package pe.edu.upc.predictivemaintain.iam.application.queryservices;

import pe.edu.upc.predictivemaintain.iam.domain.model.aggregates.UserAccount;
import pe.edu.upc.predictivemaintain.iam.domain.model.queries.GetUserAccountByIdQuery;

public interface UserAccountQueryService {

    UserAccount handle(GetUserAccountByIdQuery query);
}