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
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import pe.edu.upc.predictivemaintain.shared.domain.model.valueobjects.PageQuery;
import pe.edu.upc.predictivemaintain.shared.domain.model.valueobjects.PagedResult;
import java.util.ArrayList;

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

    @Override
    public PagedResult<UserAccount> search(UUID tenantId, RoleName role, Boolean active, PageQuery pageQuery) {
        Specification<UserAccountPersistenceEntity> specification = (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(builder.equal(root.get("tenantId"), tenantId));
            if (active != null) {
                predicates.add(builder.equal(root.get("active"), active));
            }
            if (role != null) {
                // The roles are a collection: join it, and make the result distinct so a user is not repeated
                Join<UserAccountPersistenceEntity, RoleName> roles = root.join("roles");
                predicates.add(builder.equal(roles, role));
                query.distinct(true);
            }
            return builder.and(predicates.toArray(new Predicate[0]));
        };

        // The second sort key (id) keeps pages stable when two users have the same name
        Page<UserAccountPersistenceEntity> page = jpaRepository.findAll(specification,
                PageRequest.of(pageQuery.page(), pageQuery.size(), Sort.by("displayName", "id")));
        return new PagedResult<>(
                page.getContent().stream().map(UserAccountPersistenceAssembler::toDomain).toList(),
                page.getTotalElements(),
                pageQuery.page(),
                pageQuery.size());
    }
}
