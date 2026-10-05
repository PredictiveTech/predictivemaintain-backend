package pe.edu.upc.predictivemaintain.iam.interfaces.rest.transform;

import pe.edu.upc.predictivemaintain.iam.application.commandservices.LoginResult;
import pe.edu.upc.predictivemaintain.iam.domain.model.aggregates.UserAccount;
import pe.edu.upc.predictivemaintain.iam.interfaces.rest.resources.AuthTokenResource;

import java.util.List;

public final class AuthTokenResourceFromResultAssembler {

    private AuthTokenResourceFromResultAssembler() {
    }

    public static AuthTokenResource toResource(LoginResult result) {
        UserAccount user = result.user();
        List<String> roles = user.getRoles().stream().map(Enum::name).sorted().toList();
        return new AuthTokenResource(
                result.token().accessToken(),
                "Bearer",
                result.token().expiresAt(),
                user.getId(),
                user.getTenantId(),
                roles);
    }
}