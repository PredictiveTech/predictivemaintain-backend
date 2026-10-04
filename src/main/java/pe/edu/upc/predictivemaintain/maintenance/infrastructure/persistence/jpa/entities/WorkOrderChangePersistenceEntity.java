package pe.edu.upc.predictivemaintain.maintenance.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.valueobjects.WorkOrderStatus;
import pe.edu.upc.predictivemaintain.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;

import java.time.Instant;
import java.util.UUID;

/**
 * Database representation of WorkOrderChange (table work_order_changes). Rows are only inserted.
 */
@Entity
@Table(name = "work_order_changes",
        indexes = @Index(name = "idx_work_order_changes_order", columnList = "tenant_id, work_order_id"))
public class WorkOrderChangePersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Id
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "work_order_id", nullable = false)
    private UUID workOrderId;

    @Column(name = "actor_id", nullable = false)
    private UUID actorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", length = 20)
    private WorkOrderStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false, length = 20)
    private WorkOrderStatus newStatus;

    @Column(name = "previous_assignee_id")
    private UUID previousAssigneeId;

    @Column(name = "new_assignee_id")
    private UUID newAssigneeId;

    @Column(name = "changed_at", nullable = false)
    private Instant changedAt;

    public WorkOrderChangePersistenceEntity() {
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

    public UUID getWorkOrderId() {
        return workOrderId;
    }

    public void setWorkOrderId(UUID workOrderId) {
        this.workOrderId = workOrderId;
    }

    public UUID getActorId() {
        return actorId;
    }

    public void setActorId(UUID actorId) {
        this.actorId = actorId;
    }

    public WorkOrderStatus getPreviousStatus() {
        return previousStatus;
    }

    public void setPreviousStatus(WorkOrderStatus previousStatus) {
        this.previousStatus = previousStatus;
    }

    public WorkOrderStatus getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(WorkOrderStatus newStatus) {
        this.newStatus = newStatus;
    }

    public UUID getPreviousAssigneeId() {
        return previousAssigneeId;
    }

    public void setPreviousAssigneeId(UUID previousAssigneeId) {
        this.previousAssigneeId = previousAssigneeId;
    }

    public UUID getNewAssigneeId() {
        return newAssigneeId;
    }

    public void setNewAssigneeId(UUID newAssigneeId) {
        this.newAssigneeId = newAssigneeId;
    }

    public Instant getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(Instant changedAt) {
        this.changedAt = changedAt;
    }
}