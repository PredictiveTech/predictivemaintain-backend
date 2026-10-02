package pe.edu.upc.predictivemaintain.subscription.application.commandservices;

import pe.edu.upc.predictivemaintain.subscription.domain.model.aggregates.Company;
import pe.edu.upc.predictivemaintain.subscription.domain.model.commands.ProvisionCompanyCommand;

/**
 * Write use cases for companies.
 */
public interface CompanyCommandService {

    /**
     * Creates the company and its initial subscription. Idempotent: if the registrationId
     * was already used, it returns the existing company.
     */
    Company handle(ProvisionCompanyCommand command);
}