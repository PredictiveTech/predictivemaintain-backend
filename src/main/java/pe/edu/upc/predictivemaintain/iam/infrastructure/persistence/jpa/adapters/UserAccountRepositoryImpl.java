package pe.edu.upc.predictivemaintain.iam.infrastructure.persistence.jpa.adapters;

import org.springframework.stereotype.Repository;
import pe.edu.upc.predictivemaintain.iam.domain.model.aggregates.UserAccount;
import pe.edu.upc.predictivemaintain.iam.domain.model.valueobjects.EmailAddress;
import pe.edu.upc.predictivemaintain.iam.domain.model.valueobjects.RoleName;
import pe.edu.upc.predictivemaintain.iam.domain.repositories.UserAccountRepository;
import pe.edu.upc.predictivemaintain.iam.infrastructure.persistence.jpa.assemblers.UserAccountPersistenceAssembler;
import pe.edu.upc.predictivemaintain.iam.infrastructure.persistence.jpa.entities.UserAccountPersistenceEntity;
import pe.edu.upc.predictivemaintain.iam.infrastructure.persistence.jpa.repositories.UserAccountPersistenceRepository;
import pe.edu.upc.predictivemaintain.shared.domain.services.DomainEventPublisher;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

@Repository
public class UserAccountRepositoryImpl implements UserAccountRepository {

    private final UserAccountPersistenceRepository jpaRepository;
    private final DomainEventPublisher eventPublisher;

    public UserAccountRepositoryImpl(UserAccountPersistenceRepository jpaRepository,
                                     DomainEventPublisher eventPublisher) {
        this.jpaRepository = jpaRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public UserAccount save(UserAccount user) {
        UserAccountPersistenceEntity entity = jpaRepository.findById(user.getId())
                .orElseGet(UserAccountPersistenceEntity::new);
        UserAccountPersistenceAssembler.copyToEntity(user, entity);
        UserAccountPersistenceEntity saved = jpaRepository.save(entity);
        eventPublisher.publishAll(user);
        return UserAccountPersistenceAssembler.toDomain(saved);
    }

    @Override
    public Optional<UserAccount> findById(UUID id) {
        return jpaRepository.findById(id).map(UserAccountPersistenceAssembler::toDomain);
    }

    @Override
    public Optional<UserAccount> findByIdAndTenantId(UUID id, UUID tenantId) {
        return jpaRepository.findByIdAndTenantId(id, tenantId).map(UserAccountPersistenceAssembler::toDomain);
    }

    @Override
    public Optional<UserAccount> findByEmail(EmailAddress email) {
        return jpaRepository.findByEmail(email.value()).map(UserAccountPersistenceAssembler::toDomain);
    }

    @Override
    public boolean existsByEmail(EmailAddress email) {
        return jpaRepository.existsByEmail(email.value());
    }

    @Override
    public long countActiveByTenantIdAndRole(UUID tenantId, RoleName role) {
        return jpaRepository.countActiveByTenantIdAndRole(tenantId, role);
    }

    @Override
    public List<UserAccount> findActiveByTenantIdAndRole(UUID tenantId, RoleName role) {
        return jpaRepository.findActiveByTenantIdAndRole(tenantId, role).stream()
                .map(UserAccountPersistenceAssembler::toDomain)
                .toList();
    }
}