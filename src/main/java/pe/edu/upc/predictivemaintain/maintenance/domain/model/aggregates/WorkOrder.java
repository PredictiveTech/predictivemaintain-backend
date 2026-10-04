package pe.edu.upc.predictivemaintain.maintenance.domain.model.aggregates;

import pe.edu.upc.predictivemaintain.maintenance.domain.model.valueobjects.WorkOrderStatus;
import pe.edu.upc.predictivemaintain.shared.domain.exceptions.DomainConflictException;
import pe.edu.upc.predictivemaintain.shared.domain.exceptions.DomainValidationException;
import pe.edu.upc.predictivemaintain.shared.domain.model.aggregates.AbstractVersionedAggregateRoot;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Maintenance intervention that answers one confirmed alert. Every change of state or technician
 * adds an entry to the history, which the repository saves together with the order.
 */
public class WorkOrder extends AbstractVersionedAggregateRoot {

    private static final int MAX_SUMMARY_LENGTH = 2000;
    private static final int MAX_REASON_LENGTH = 500;

    private final UUID id;
    private final UUID tenantId;
    private final UUID alertId;
    private UUID assignedUserId;
    private WorkOrderStatus status;
    private String summary;
    private final Instant openedAt;
    private Instant completedAt;
    private final List<WorkOrderChange> pendingChanges = new ArrayList<>();

    private WorkOrder(UUID id, UUID tenantId, UUID alertId, UUID assignedUserId, WorkOrderStatus status,
                      String summary, Instant openedAt, Instant completedAt, long version) {
        super(version);
        this.id = Objects.requireNonNull(id);
        this.tenantId = Objects.requireNonNull(tenantId);
        this.alertId = Objects.requireNonNull(alertId);
        this.assignedUserId = assignedUserId;
        this.status = Objects.requireNonNull(status);
        this.summary = summary;
        this.openedAt = Objects.requireNonNull(openedAt);
        this.completedAt = completedAt;
    }

    public static WorkOrder open(UUID tenantId, UUID alertId, UUID actorId, Instant now) {
        WorkOrder order = new WorkOrder(UUID.randomUUID(), tenantId, alertId, null,
                WorkOrderStatus.OPEN, null, now, null, 0);
        order.recordChange(actorId, null, null, now);
        return order;
    }

    public static WorkOrder restore(UUID id, UUID tenantId, UUID alertId, UUID assignedUserId,
                                    WorkOrderStatus status, String summary, Instant openedAt,
                                    Instant completedAt, long version) {
        return new WorkOrder(id, tenantId, alertId, assignedUserId, status, summary, openedAt, completedAt, version);
    }

    /** Assigns (from OPEN) or reassigns (from ASSIGNED) the order. Not possible once the work has started. */
    public void assign(UUID technicianId, UUID actorId, Instant now) {
        if (status != WorkOrderStatus.OPEN && status != WorkOrderStatus.ASSIGNED) {
            throw invalidTransition(WorkOrderStatus.ASSIGNED);
        }
        WorkOrderStatus previousStatus = status;
        UUID previousAssignee = assignedUserId;
        this.assignedUserId = Objects.requireNonNull(technicianId);
        this.status = WorkOrderStatus.ASSIGNED;
        recordChange(actorId, previousStatus, previousAssignee, now);
    }

    public void start(UUID actorId, Instant now) {
        if (status != WorkOrderStatus.ASSIGNED) {
            throw invalidTransition(WorkOrderStatus.IN_PROGRESS);
        }
        WorkOrderStatus previousStatus = status;
        this.status = WorkOrderStatus.IN_PROGRESS;
        recordChange(actorId, previousStatus, assignedUserId, now);
    }

    /** The corrective actions are mandatory to close the order. */
    public void complete(String correctiveActions, UUID actorId, Instant now) {
        if (status != WorkOrderStatus.IN_PROGRESS) {
            throw invalidTransition(WorkOrderStatus.COMPLETED);
        }
        String validSummary = requireText(correctiveActions, MAX_SUMMARY_LENGTH,
                "validation.work-order.summary-required");
        WorkOrderStatus previousStatus = status;
        this.summary = validSummary;
        this.completedAt = now;
        this.status = WorkOrderStatus.COMPLETED;
        recordChange(actorId, previousStatus, assignedUserId, now);
    }

    /** Only before the work starts, and with a reason (stored in the summary field). */
    public void cancel(String reason, UUID actorId, Instant now) {
        if (status != WorkOrderStatus.OPEN && status != WorkOrderStatus.ASSIGNED) {
            throw invalidTransition(WorkOrderStatus.CANCELLED);
        }
        String validReason = requireText(reason, MAX_REASON_LENGTH, "validation.work-order.cancel-reason-required");
        WorkOrderStatus previousStatus = status;
        this.summary = validReason;
        this.status = WorkOrderStatus.CANCELLED;
        recordChange(actorId, previousStatus, assignedUserId, now);
    }

    public boolean isAssignedTo(UUID userId) {
        return assignedUserId != null && assignedUserId.equals(userId);
    }

    /** Returns the history entries not saved yet and clears them. */
    public List<WorkOrderChange> drainChanges() {
        List<WorkOrderChange> changes = List.copyOf(pendingChanges);
        pendingChanges.clear();
        return changes;
    }

    private void recordChange(UUID actorId, WorkOrderStatus previousStatus, UUID previousAssigneeId, Instant now) {
        pendingChanges.add(WorkOrderChange.create(tenantId, id, actorId, previousStatus, status,
                previousAssigneeId, assignedUserId, now));
    }

    private DomainConflictException invalidTransition(WorkOrderStatus target) {
        return new DomainConflictException("conflict.work-order.invalid-transition", status.name(), target.name());
    }

    private static String requireText(String value, int maxLength, String errorKey) {
        if (value == null || value.isBlank() || value.trim().length() > maxLength) {
            throw new DomainValidationException(errorKey);
        }
        return value.trim();
    }

    public UUID getId() {
        return id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public UUID getAlertId() {
        return alertId;
    }

    public UUID getAssignedUserId() {
        return assignedUserId;
    }

    public WorkOrderStatus getStatus() {
        return status;
    }

    public String getSummary() {
        return summary;
    }

    public Instant getOpenedAt() {
        return openedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }
}