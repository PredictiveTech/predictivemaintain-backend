package pe.edu.upc.predictivemaintain.iam.infrastructure.persistence.jpa.adapters;

import org.springframework.stereotype.Repository;
import pe.edu.upc.predictivemaintain.iam.domain.model.aggregates.PasswordResetRequest;
import pe.edu.upc.predictivemaintain.iam.domain.repositories.PasswordResetRequestRepository;
import pe.edu.upc.predictivemaintain.iam.infrastructure.persistence.jpa.assemblers.PasswordResetRequestPersistenceAssembler;
import pe.edu.upc.predictivemaintain.iam.infrastructure.persistence.jpa.entities.PasswordResetRequestPersistenceEntity;
import pe.edu.upc.predictivemaintain.iam.infrastructure.persistence.jpa.repositories.PasswordResetRequestPersistenceRepository;
import pe.edu.upc.predictivemaintain.shared.domain.services.DomainEventPublisher;

import java.util.Optional;

@Repository
public class PasswordResetRequestRepositoryImpl implements PasswordResetRequestRepository {

    private final PasswordResetRequestPersistenceRepository jpaRepository;
    private final DomainEventPublisher eventPublisher;

    public PasswordResetRequestRepositoryImpl(PasswordResetRequestPersistenceRepository jpaRepository,
                                              DomainEventPublisher eventPublisher) {
        this.jpaRepository = jpaRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public PasswordResetRequest save(PasswordResetRequest request) {
        PasswordResetRequestPersistenceEntity entity = jpaRepository.findById(request.getId())
                .orElseGet(PasswordResetRequestPersistenceEntity::new);
        PasswordResetRequestPersistenceAssembler.copyToEntity(request, entity);
        PasswordResetRequestPersistenceEntity saved = jpaRepository.save(entity);
        eventPublisher.publishAll(request);
        return PasswordResetRequestPersistenceAssembler.toDomain(saved);
    }

    @Override
    public Optional<PasswordResetRequest> findByTokenHash(String tokenHash) {
        return jpaRepository.findByTokenHash(tokenHash).map(PasswordResetRequestPersistenceAssembler::toDomain);
    }
}