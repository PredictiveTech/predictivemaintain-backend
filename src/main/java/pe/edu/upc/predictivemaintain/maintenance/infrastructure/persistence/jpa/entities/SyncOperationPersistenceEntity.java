package pe.edu.upc.predictivemaintain.maintenance.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.valueobjects.SyncOperationType;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.valueobjects.SyncOutcome;
import pe.edu.upc.predictivemaintain.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;

import java.time.Instant;
import java.util.UUID;

/**
 * Database representation of SyncOperationRecord (table sync_operations). The unique pair
 * (tenant_id, operation_id) is what guarantees an offline action is applied at most once.
 */
@Entity
@Table(name = "sync_operations",
        uniqueConstraints = @UniqueConstraint(name = "uk_sync_tenant_operation",
                columnNames = {"tenant_id", "operation_id"}))
public class SyncOperationPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Id
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "operation_id", nullable = false)
    private UUID operationId;

    @Column(name = "work_order_id", nullable = false)
    private UUID workOrderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SyncOperationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SyncOutcome outcome;

    @Column(name = "action_time", nullable = false)
    private Instant actionTime;

    @Column(name = "processed_at", nullable = false)
    private Instant processedAt;

    public SyncOperationPersistenceEntity() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }

    public UUID getOperationId() {
        return operationId;
    }

    public void setOperationId(UUID operationId) {
        this.operationId = operationId;
    }

    public UUID getWorkOrderId() {
        return workOrderId;
    }

    public void setWorkOrderId(UUID workOrderId) {
        this.workOrderId = workOrderId;
    }

    public SyncOperationType getType() {
        return type;
    }

    public void setType(SyncOperationType type) {
        this.type = type;
    }

    public SyncOutcome getOutcome() {
        return outcome;
    }

    public void setOutcome(SyncOutcome outcome) {
        this.outcome = outcome;
    }

    public Instant getActionTime() {
        return actionTime;
    }

    public void setActionTime(Instant actionTime) {
        this.actionTime = actionTime;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(Instant processedAt) {
        this.processedAt = processedAt;
    }
}