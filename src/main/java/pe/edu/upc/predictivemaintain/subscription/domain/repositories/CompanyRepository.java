package pe.edu.upc.predictivemaintain.subscription.domain.repositories;

import pe.edu.upc.predictivemaintain.subscription.domain.model.aggregates.Company;

import java.util.Optional;
import java.util.UUID;

public interface CompanyRepository {

    Company save(Company company);

    Optional<Company> findById(UUID id);

    Optional<Company> findByRegistrationId(UUID registrationId);
}