package pe.edu.upc.predictivemaintain.subscription.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.predictivemaintain.subscription.infrastructure.persistence.jpa.entities.CompanyPersistenceEntity;

import java.util.Optional;
import java.util.UUID;

public interface CompanyPersistenceRepository extends JpaRepository<CompanyPersistenceEntity, UUID> {

    Optional<CompanyPersistenceEntity> findByRegistrationId(UUID registrationId);
}