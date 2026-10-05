package pe.edu.upc.predictivemaintain.maintenance.application.internal.commandservices;

import org.springframework.stereotype.Service;
import pe.edu.upc.predictivemaintain.maintenance.application.commandservices.SyncResult;
import pe.edu.upc.predictivemaintain.maintenance.application.commandservices.WorkOrderCommandService;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.aggregates.SyncOperationRecord;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.aggregates.WorkOrder;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.aggregates.WorkOrderChange;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.commands.CompleteWorkOrderCommand;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.commands.StartWorkOrderCommand;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.commands.SyncOperation;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.valueobjects.SyncOperationType;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.valueobjects.SyncOutcome;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.valueobjects.WorkOrderStatus;
import pe.edu.upc.predictivemaintain.maintenance.domain.repositories.SyncOperationRepository;
import pe.edu.upc.predictivemaintain.maintenance.domain.repositories.WorkOrderRepository;
import pe.edu.upc.predictivemaintain.maintenance.domain.services.SyncConflictPolicy;
import pe.edu.upc.predictivemaintain.shared.application.errors.ApplicationException;
import pe.edu.upc.predictivemaintain.shared.domain.exceptions.DomainConflictException;
import pe.edu.upc.predictivemaintain.shared.domain.exceptions.DomainValidationException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Decides and applies ONE offline action.
 *
 * <p>It is deliberately NOT @Transactional. Each step (apply the action, remember it) is its own transaction.
 * If the whole method were one transaction, an action refused by the state machine would throw through
 * {@link WorkOrderCommandService}, which would mark the shared transaction as "rollback only" even though we
 * catch the exception here, and the final commit would fail with UnexpectedRollbackException, losing the
 * record of what happened. Splitting them avoids that, and it is safe because the actions are idempotent by
 * state: repeating START on an order already in progress, or COMPLETE on a completed one, changes nothing.
 */
@Service
public class SyncOperationProcessor {

    private final SyncOperationRepository syncOperationRepository;
    private final WorkOrderRepository workOrderRepository;
    private final WorkOrderCommandService workOrderCommandService;

    public SyncOperationProcessor(SyncOperationRepository syncOperationRepository,
                                  WorkOrderRepository workOrderRepository,
                                  WorkOrderCommandService workOrderCommandService) {
        this.syncOperationRepository = syncOperationRepository;
        this.workOrderRepository = workOrderRepository;
        this.workOrderCommandService = workOrderCommandService;
    }

    /**
     * @param actionTime when the technician acted, already translated to server time
     */
    public SyncResult.Item process(UUID tenantId, UUID actorId, SyncOperation operation,
                                   Instant actionTime, Instant serverNow) {
        // Idempotency: an action already processed answers with what happened the first time.
        Optional<SyncOperationRecord> previous =
                syncOperationRepository.findByTenantIdAndOperationId(tenantId, operation.operationId());
        if (previous.isPresent()) {
            return report(tenantId, actorId, operation, SyncOutcome.ALREADY_APPLIED, "sync.duplicate",
                    previous.get().outcome().name());
        }

        Decision decision = decide(tenantId, actorId, operation, actionTime, serverNow);
        syncOperationRepository.save(SyncOperationRecord.create(tenantId, operation.operationId(),
                operation.workOrderId(), operation.type(), decision.outcome(), actionTime, serverNow));
        return report(tenantId, actorId, operation, decision.outcome(), decision.messageKey(), decision.args());
    }

    private Decision decide(UUID tenantId, UUID actorId, SyncOperation operation,
                            Instant actionTime, Instant serverNow) {
        if (SyncConflictPolicy.isTooOld(actionTime, serverNow)) {
            return Decision.of(SyncOutcome.REJECTED, "sync.rejected-too-old",
                    SyncConflictPolicy.MAX_AGE.toDays());
        }
        Optional<WorkOrder> found = workOrderRepository.findByIdAndTenantId(operation.workOrderId(), tenantId);
        if (found.isEmpty()) {
            return Decision.of(SyncOutcome.REJECTED, "sync.rejected-not-found");
        }
        WorkOrder order = found.get();
        // Checked before anything else, so a technician learns nothing about orders that are not his.
        if (!order.isAssignedTo(actorId)) {
            return Decision.of(SyncOutcome.REJECTED, "sync.rejected-not-assigned");
        }

        // US-28, scenario 3: the most recent record wins.
        Instant lastChange = lastChangeOf(tenantId, order);
        if (SyncConflictPolicy.remoteIsMoreRecent(lastChange, actionTime)) {
            return Decision.of(SyncOutcome.SUPERSEDED, "sync.superseded-newer", order.getStatus().name(), lastChange);
        }

        if (isAlreadyInTargetState(order, operation.type())) {
            return Decision.of(SyncOutcome.ALREADY_APPLIED, "sync.already-in-state", order.getStatus().name());
        }

        try {
            if (operation.type() == SyncOperationType.START) {
                workOrderCommandService.handle(new StartWorkOrderCommand(
                        tenantId, actorId, order.getId(), null, actionTime));
            } else {
                workOrderCommandService.handle(new CompleteWorkOrderCommand(
                        tenantId, actorId, order.getId(), operation.summary(), null, actionTime));
            }
            return Decision.of(SyncOutcome.APPLIED, "sync.applied");
        } catch (DomainValidationException ex) {
            return new Decision(SyncOutcome.REJECTED, ex.getMessageKey(), ex.getArguments());
        } catch (DomainConflictException ex) {
            // The state machine forbids it (for example COMPLETE on an order that was never started).
            return Decision.of(SyncOutcome.SUPERSEDED, "sync.superseded-state",
                    order.getStatus().name(), operation.type().name());
        } catch (ApplicationException ex) {
            return new Decision(SyncOutcome.REJECTED, ex.getErrorCode().messageKey(), ex.getArguments());
        }
    }

    private static boolean isAlreadyInTargetState(WorkOrder order, SyncOperationType type) {
        return (type == SyncOperationType.START && order.getStatus() == WorkOrderStatus.IN_PROGRESS)
                || (type == SyncOperationType.COMPLETE && order.getStatus() == WorkOrderStatus.COMPLETED);
    }

    private Instant lastChangeOf(UUID tenantId, WorkOrder order) {
        List<WorkOrderChange> history = workOrderRepository.findHistory(tenantId, order.getId());
        return history.isEmpty() ? null : history.get(history.size() - 1).changedAt();
    }

    /** Builds the answer with the order's CURRENT state, so the app can refresh its local copy. */
    private SyncResult.Item report(UUID tenantId, UUID actorId, SyncOperation operation, SyncOutcome outcome,
                                   String messageKey, Object... args) {
        WorkOrder order = workOrderRepository.findByIdAndTenantId(operation.workOrderId(), tenantId)
                .filter(candidate -> candidate.isAssignedTo(actorId))
                .orElse(null);
        return new SyncResult.Item(operation.operationId(), outcome, messageKey, args,
                order == null ? null : order.getStatus(), order == null ? null : order.getVersion());
    }

    private record Decision(SyncOutcome outcome, String messageKey, Object[] args) {
        static Decision of(SyncOutcome outcome, String messageKey, Object... args) {
            return new Decision(outcome, messageKey, args);
        }
    }
}