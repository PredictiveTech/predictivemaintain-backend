package pe.edu.upc.predictivemaintain.subscription.infrastructure.persistence.jpa.assemblers;

import pe.edu.upc.predictivemaintain.subscription.domain.model.aggregates.Company;
import pe.edu.upc.predictivemaintain.subscription.infrastructure.persistence.jpa.entities.CompanyPersistenceEntity;

public final class CompanyPersistenceAssembler {

    private CompanyPersistenceAssembler() {
    }

    public static Company toDomain(CompanyPersistenceEntity entity) {
        return Company.restore(entity.getId(), entity.getName(), entity.getRegistrationId(), entity.isActive());
    }

    public static void copyToEntity(Company company, CompanyPersistenceEntity entity) {
        entity.setId(company.getId());
        entity.setName(company.getName());
        entity.setRegistrationId(company.getRegistrationId());
        entity.setActive(company.isActive());
    }
}