package pe.edu.upc.predictivemaintain.iam.application.commandservices;

import pe.edu.upc.predictivemaintain.iam.domain.model.commands.LoginCommand;
import pe.edu.upc.predictivemaintain.iam.domain.model.commands.RegisterCompanyAccountCommand;
import pe.edu.upc.predictivemaintain.iam.domain.model.commands.RequestPasswordResetCommand;
import pe.edu.upc.predictivemaintain.iam.domain.model.commands.ResetPasswordCommand;

/**
 * Use cases that do not require an authenticated user.
 */
public interface AuthenticationCommandService {

    RegisterCompanyAccountResult handle(RegisterCompanyAccountCommand command);

    LoginResult handle(LoginCommand command);

    void handle(RequestPasswordResetCommand command);

    void handle(ResetPasswordCommand command);
}