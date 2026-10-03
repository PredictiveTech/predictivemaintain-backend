package pe.edu.upc.predictivemaintain.iam.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.predictivemaintain.iam.infrastructure.persistence.jpa.entities.PasswordResetRequestPersistenceEntity;

import java.util.Optional;
import java.util.UUID;

public interface PasswordResetRequestPersistenceRepository
        extends JpaRepository<PasswordResetRequestPersistenceEntity, UUID> {

    Optional<PasswordResetRequestPersistenceEntity> findByTokenHash(String tokenHash);
}