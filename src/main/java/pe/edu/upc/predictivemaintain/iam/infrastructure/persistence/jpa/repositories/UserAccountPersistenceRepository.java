package pe.edu.upc.predictivemaintain.iam.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.upc.predictivemaintain.iam.domain.model.valueobjects.RoleName;
import pe.edu.upc.predictivemaintain.iam.infrastructure.persistence.jpa.entities.UserAccountPersistenceEntity;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;
import java.util.List;


/**
 * JpaSpecificationExecutor lets the adapter build the list filters dynamically.
 */
public interface UserAccountPersistenceRepository
        extends JpaRepository<UserAccountPersistenceEntity, UUID>, JpaSpecificationExecutor<UserAccountPersistenceEntity> {

    Optional<UserAccountPersistenceEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<UserAccountPersistenceEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    @Query("select count(u) from UserAccountPersistenceEntity u join u.roles r "
            + "where u.tenantId = :tenantId and u.active = true and r = :role")
    long countActiveByTenantIdAndRole(@Param("tenantId") UUID tenantId, @Param("role") RoleName role);

    @Query("select distinct u from UserAccountPersistenceEntity u join u.roles r "
            + "where u.tenantId = :tenantId and u.active = true and r = :role")
    List<UserAccountPersistenceEntity> findActiveByTenantIdAndRole(@Param("tenantId") UUID tenantId,
                                                                   @Param("role") RoleName role);
}