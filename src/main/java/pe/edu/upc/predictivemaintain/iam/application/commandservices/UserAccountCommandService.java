package pe.edu.upc.predictivemaintain.iam.application.commandservices;

import pe.edu.upc.predictivemaintain.iam.domain.model.aggregates.UserAccount;
import pe.edu.upc.predictivemaintain.iam.domain.model.commands.ChangeUserRolesCommand;
import pe.edu.upc.predictivemaintain.iam.domain.model.commands.CreateUserCommand;
import pe.edu.upc.predictivemaintain.iam.domain.model.commands.DeactivateUserCommand;
import pe.edu.upc.predictivemaintain.iam.domain.model.commands.UpdateProfileCommand;

/**
 * Use cases for authenticated users and for managers administering their company.
 */
public interface UserAccountCommandService {

    UserAccount handle(UpdateProfileCommand command);

    UserAccount handle(CreateUserCommand command);

    UserAccount handle(ChangeUserRolesCommand command);

    UserAccount handle(DeactivateUserCommand command);
}