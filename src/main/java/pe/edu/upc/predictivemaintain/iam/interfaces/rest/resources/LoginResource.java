package pe.edu.upc.predictivemaintain.iam.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;

public record LoginResource(@NotBlank String email, @NotBlank String password) {
}