package pe.edu.upc.predictivemaintain.subscription.infrastructure.persistence.jpa.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.predictivemaintain.subscription.infrastructure.persistence.jpa.entities.InvoicePersistenceEntity;

import java.util.UUID;

public interface InvoicePersistenceRepository extends JpaRepository<InvoicePersistenceEntity, UUID> {

    Page<InvoicePersistenceEntity> findByTenantId(UUID tenantId, Pageable pageable);
}