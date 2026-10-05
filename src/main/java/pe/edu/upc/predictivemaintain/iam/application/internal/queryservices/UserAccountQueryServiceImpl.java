package pe.edu.upc.predictivemaintain.iam.application.internal.queryservices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.predictivemaintain.iam.application.errors.IamError;
import pe.edu.upc.predictivemaintain.iam.application.queryservices.UserAccountQueryService;
import pe.edu.upc.predictivemaintain.iam.domain.model.aggregates.UserAccount;
import pe.edu.upc.predictivemaintain.iam.domain.model.queries.GetUserAccountByIdQuery;
import pe.edu.upc.predictivemaintain.iam.domain.repositories.UserAccountRepository;
import pe.edu.upc.predictivemaintain.shared.application.errors.ApplicationException;

@Service
public class UserAccountQueryServiceImpl implements UserAccountQueryService {

    private final UserAccountRepository userAccountRepository;

    public UserAccountQueryServiceImpl(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserAccount handle(GetUserAccountByIdQuery query) {
        return userAccountRepository.findById(query.userId())
                .orElseThrow(() -> new ApplicationException(IamError.USER_NOT_FOUND));
    }
}