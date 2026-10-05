package pe.edu.upc.predictivemaintain.iam.interfaces.rest.transform;

import pe.edu.upc.predictivemaintain.iam.domain.model.commands.ChangeUserRolesCommand;
import pe.edu.upc.predictivemaintain.iam.domain.model.commands.CreateUserCommand;
import pe.edu.upc.predictivemaintain.iam.domain.model.commands.LoginCommand;
import pe.edu.upc.predictivemaintain.iam.domain.model.commands.RegisterCompanyAccountCommand;
import pe.edu.upc.predictivemaintain.iam.domain.model.commands.RequestPasswordResetCommand;
import pe.edu.upc.predictivemaintain.iam.domain.model.commands.ResetPasswordCommand;
import pe.edu.upc.predictivemaintain.iam.domain.model.commands.UpdateProfileCommand;
import pe.edu.upc.predictivemaintain.iam.interfaces.rest.resources.ChangeRolesResource;
import pe.edu.upc.predictivemaintain.iam.interfaces.rest.resources.CreateUserResource;
import pe.edu.upc.predictivemaintain.iam.interfaces.rest.resources.LoginResource;
import pe.edu.upc.predictivemaintain.iam.interfaces.rest.resources.PasswordResetRequestResource;
import pe.edu.upc.predictivemaintain.iam.interfaces.rest.resources.RegisterCompanyResource;
import pe.edu.upc.predictivemaintain.iam.interfaces.rest.resources.ResetPasswordResource;
import pe.edu.upc.predictivemaintain.iam.interfaces.rest.resources.UpdateProfileResource;

import java.util.UUID;

/**
 * Converts request resources into domain commands. The tenantId and userId always arrive as
 * separate arguments taken from the authenticated identity, never from the resource.
 */
public final class IamCommandFromResourceAssembler {

    private IamCommandFromResourceAssembler() {
    }

    public static RegisterCompanyAccountCommand toCommand(RegisterCompanyResource resource) {
        return new RegisterCompanyAccountCommand(
                resource.companyName(), resource.email(), resource.password(), resource.registrationId());
    }

    public static LoginCommand toCommand(LoginResource resource) {
        return new LoginCommand(resource.email(), resource.password());
    }

    public static RequestPasswordResetCommand toCommand(PasswordResetRequestResource resource) {
        return new RequestPasswordResetCommand(resource.email());
    }

    public static ResetPasswordCommand toCommand(ResetPasswordResource resource) {
        return new ResetPasswordCommand(resource.token(), resource.newPassword());
    }

    public static UpdateProfileCommand toCommand(UUID userId, UpdateProfileResource resource) {
        return new UpdateProfileCommand(userId, resource.displayName());
    }

    public static CreateUserCommand toCommand(UUID tenantId, CreateUserResource resource) {
        return new CreateUserCommand(tenantId, resource.email(), resource.displayName(),
                resource.initialPassword(), resource.roles());
    }

    public static ChangeUserRolesCommand toCommand(UUID tenantId, UUID userId, ChangeRolesResource resource) {
        return new ChangeUserRolesCommand(tenantId, userId, resource.roles());
    }
}