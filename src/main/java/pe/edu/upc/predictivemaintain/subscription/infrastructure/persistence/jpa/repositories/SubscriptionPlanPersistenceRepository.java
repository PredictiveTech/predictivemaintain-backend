package pe.edu.upc.predictivemaintain.subscription.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.predictivemaintain.subscription.infrastructure.persistence.jpa.entities.SubscriptionPlanPersistenceEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data repository. Only the adapter below uses it.
 */
public interface SubscriptionPlanPersistenceRepository extends JpaRepository<SubscriptionPlanPersistenceEntity, UUID> {

    Optional<SubscriptionPlanPersistenceEntity> findByName(String name);

    boolean existsByName(String name);

    List<SubscriptionPlanPersistenceEntity> findAllByOrderByAssetLimitAsc();
}