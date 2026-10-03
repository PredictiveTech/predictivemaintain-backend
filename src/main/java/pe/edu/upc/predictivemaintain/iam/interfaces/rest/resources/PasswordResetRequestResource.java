package pe.edu.upc.predictivemaintain.iam.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;

public record PasswordResetRequestResource(@NotBlank String email) {
}