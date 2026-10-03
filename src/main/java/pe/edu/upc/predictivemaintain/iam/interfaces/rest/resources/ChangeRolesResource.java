package pe.edu.upc.predictivemaintain.iam.interfaces.rest.resources;

import jakarta.validation.constraints.NotEmpty;
import pe.edu.upc.predictivemaintain.iam.domain.model.valueobjects.RoleName;

import java.util.Set;

public record ChangeRolesResource(@NotEmpty Set<RoleName> roles) {
}