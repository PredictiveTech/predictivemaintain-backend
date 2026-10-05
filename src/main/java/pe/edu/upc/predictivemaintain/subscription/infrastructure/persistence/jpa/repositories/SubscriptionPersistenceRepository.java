package pe.edu.upc.predictivemaintain.subscription.infrastructure.persistence.jpa.repositories;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.upc.predictivemaintain.subscription.infrastructure.persistence.jpa.entities.SubscriptionPersistenceEntity;
import pe.edu.upc.predictivemaintain.subscription.domain.model.valueobjects.SubscriptionStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.Instant;

public interface SubscriptionPersistenceRepository extends JpaRepository<SubscriptionPersistenceEntity, UUID> {

    Optional<SubscriptionPersistenceEntity> findFirstByTenantIdOrderByStartsAtDesc(UUID tenantId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from SubscriptionPersistenceEntity s where s.tenantId = :tenantId order by s.startsAt desc")
    List<SubscriptionPersistenceEntity> findAllByTenantIdForUpdate(@Param("tenantId") UUID tenantId);

    List<SubscriptionPersistenceEntity> findByStatusAndEndsAtGreaterThanAndEndsAtLessThanEqual(
            SubscriptionStatus status, Instant from, Instant to);
}