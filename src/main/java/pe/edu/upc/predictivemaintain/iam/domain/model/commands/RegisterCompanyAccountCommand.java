package pe.edu.upc.predictivemaintain.iam.domain.model.commands;

import java.util.UUID;

public record RegisterCompanyAccountCommand(String companyName, String email, String password, UUID registrationId) {
}