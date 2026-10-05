package pe.edu.upc.predictivemaintain.subscription.infrastructure.persistence.jpa.adapters;

import org.springframework.stereotype.Repository;
import pe.edu.upc.predictivemaintain.shared.domain.services.DomainEventPublisher;
import pe.edu.upc.predictivemaintain.subscription.domain.model.aggregates.Company;
import pe.edu.upc.predictivemaintain.subscription.domain.repositories.CompanyRepository;
import pe.edu.upc.predictivemaintain.subscription.infrastructure.persistence.jpa.assemblers.CompanyPersistenceAssembler;
import pe.edu.upc.predictivemaintain.subscription.infrastructure.persistence.jpa.entities.CompanyPersistenceEntity;
import pe.edu.upc.predictivemaintain.subscription.infrastructure.persistence.jpa.repositories.CompanyPersistenceRepository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class CompanyRepositoryImpl implements CompanyRepository {

    private final CompanyPersistenceRepository jpaRepository;
    private final DomainEventPublisher eventPublisher;

    public CompanyRepositoryImpl(CompanyPersistenceRepository jpaRepository,
                                 DomainEventPublisher eventPublisher) {
        this.jpaRepository = jpaRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Company save(Company company) {
        CompanyPersistenceEntity entity = jpaRepository.findById(company.getId())
                .orElseGet(CompanyPersistenceEntity::new);
        CompanyPersistenceAssembler.copyToEntity(company, entity);
        CompanyPersistenceEntity saved = jpaRepository.save(entity);
        eventPublisher.publishAll(company);
        return CompanyPersistenceAssembler.toDomain(saved);
    }

    @Override
    public Optional<Company> findById(UUID id) {
        return jpaRepository.findById(id).map(CompanyPersistenceAssembler::toDomain);
    }

    @Override
    public Optional<Company> findByRegistrationId(UUID registrationId) {
        return jpaRepository.findByRegistrationId(registrationId).map(CompanyPersistenceAssembler::toDomain);
    }
}