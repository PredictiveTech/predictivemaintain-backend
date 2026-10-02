package pe.edu.upc.predictivemaintain.subscription.domain.model.commands;

import java.util.UUID;

/**
 * Command to create a company together with its initial subscription.
 */
public record ProvisionCompanyCommand(String companyName, UUID registrationId) {
}