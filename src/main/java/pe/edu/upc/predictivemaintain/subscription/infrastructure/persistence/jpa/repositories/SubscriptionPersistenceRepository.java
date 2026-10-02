package pe.edu.upc.predictivemaintain.subscription.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.predictivemaintain.subscription.infrastructure.persistence.jpa.entities.SubscriptionPersistenceEntity;

import java.util.Optional;
import java.util.UUID;

public interface SubscriptionPersistenceRepository extends JpaRepository<SubscriptionPersistenceEntity, UUID> {

    Optional<SubscriptionPersistenceEntity> findFirstByTenantIdOrderByStartsAtDesc(UUID tenantId);
}