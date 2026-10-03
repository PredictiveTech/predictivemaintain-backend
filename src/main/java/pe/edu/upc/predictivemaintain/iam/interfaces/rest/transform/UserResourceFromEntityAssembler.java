package pe.edu.upc.predictivemaintain.iam.interfaces.rest.transform;

import pe.edu.upc.predictivemaintain.iam.domain.model.aggregates.UserAccount;
import pe.edu.upc.predictivemaintain.iam.interfaces.rest.resources.UserResource;

import java.util.List;

public final class UserResourceFromEntityAssembler {

    private UserResourceFromEntityAssembler() {
    }

    public static UserResource toResource(UserAccount user) {
        List<String> roles = user.getRoles().stream().map(Enum::name).sorted().toList();
        return new UserResource(
                user.getId(),
                user.getTenantId(),
                user.getEmail().value(),
                user.getDisplayName().value(),
                user.isActive(),
                roles);
    }
}