package pe.edu.upc.predictivemaintain.maintenance.application.internal.commandservices;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.predictivemaintain.iam.interfaces.acl.IamContextFacade;
import pe.edu.upc.predictivemaintain.maintenance.application.commandservices.WorkOrderCommandService;
import pe.edu.upc.predictivemaintain.maintenance.application.errors.MaintenanceError;
import pe.edu.upc.predictivemaintain.maintenance.application.outboundservices.EvidenceStorage;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.aggregates.Alert;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.aggregates.EvidencePhoto;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.aggregates.WorkOrder;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.commands.AssignWorkOrderCommand;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.commands.AttachEvidenceCommand;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.commands.CancelWorkOrderCommand;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.commands.CompleteWorkOrderCommand;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.commands.CreateWorkOrderCommand;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.commands.StartWorkOrderCommand;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.valueobjects.AlertStatus;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.valueobjects.ImageFormat;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.valueobjects.WorkOrderStatus;
import pe.edu.upc.predictivemaintain.maintenance.domain.repositories.AlertRepository;
import pe.edu.upc.predictivemaintain.maintenance.domain.repositories.EvidencePhotoRepository;
import pe.edu.upc.predictivemaintain.maintenance.domain.repositories.WorkOrderRepository;
import pe.edu.upc.predictivemaintain.shared.application.errors.ApplicationException;
import pe.edu.upc.predictivemaintain.shared.application.errors.CommonError;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
public class WorkOrderCommandServiceImpl implements WorkOrderCommandService {

    private final WorkOrderRepository workOrderRepository;
    private final AlertRepository alertRepository;
    private final EvidencePhotoRepository evidenceRepository;
    private final EvidenceStorage evidenceStorage;
    private final IamContextFacade iamContextFacade;
    private final Clock clock;
    private final long maxEvidenceBytes;

    public WorkOrderCommandServiceImpl(WorkOrderRepository workOrderRepository,
                                       AlertRepository alertRepository,
                                       EvidencePhotoRepository evidenceRepository,
                                       EvidenceStorage evidenceStorage,
                                       IamContextFacade iamContextFacade,
                                       Clock clock,
                                       @Value("${app.evidence.max-size-bytes:5242880}") long maxEvidenceBytes) {
        this.workOrderRepository = workOrderRepository;
        this.alertRepository = alertRepository;
        this.evidenceRepository = evidenceRepository;
        this.evidenceStorage = evidenceStorage;
        this.iamContextFacade = iamContextFacade;
        this.clock = clock;
        this.maxEvidenceBytes = maxEvidenceBytes;
    }

    @Override
    @Transactional
    public WorkOrder handle(CreateWorkOrderCommand command) {
        Alert alert = findAlert(command.tenantId(), command.alertId());
        if (alert.getStatus() != AlertStatus.CONFIRMED) {
            throw new ApplicationException(MaintenanceError.ALERT_NOT_CONFIRMED);
        }
        if (workOrderRepository.findByAlertIdAndTenantId(command.alertId(), command.tenantId()).isPresent()) {
            throw new ApplicationException(MaintenanceError.WORK_ORDER_ALREADY_EXISTS);
        }
        return workOrderRepository.save(
                WorkOrder.open(command.tenantId(), command.alertId(), command.actorId(), clock.instant()));
    }

    @Override
    @Transactional
    public WorkOrder handle(AssignWorkOrderCommand command) {
        WorkOrder order = findOrder(command.tenantId(), command.workOrderId());
        order.assertExpectedVersion(command.expectedVersion());
        if (!iamContextFacade.isActiveTechnician(command.tenantId(), command.technicianId())) {
            throw new ApplicationException(MaintenanceError.INVALID_TECHNICIAN);
        }
        order.assign(command.technicianId(), command.actorId(), clock.instant());
        return workOrderRepository.save(order);
    }

    @Override
    @Transactional
    public WorkOrder handle(StartWorkOrderCommand command) {
        WorkOrder order = findOrder(command.tenantId(), command.workOrderId());
        requireAssignedTo(order, command.actorId());
        order.assertExpectedVersion(command.expectedVersion());
        order.start(command.actorId(), clock.instant());
        return workOrderRepository.save(order);
    }

    @Override
    @Transactional
    public WorkOrder handle(CompleteWorkOrderCommand command) {
        WorkOrder order = findOrder(command.tenantId(), command.workOrderId());
        requireAssignedTo(order, command.actorId());
        order.assertExpectedVersion(command.expectedVersion());
        order.complete(command.summary(), command.actorId(), clock.instant());
        WorkOrder saved = workOrderRepository.save(order);

        // Closing the order and resolving its alert happen in the same transaction: both or none.
        Alert alert = findAlert(command.tenantId(), order.getAlertId());
        alert.resolve();
        alertRepository.save(alert);
        return saved;
    }

    @Override
    @Transactional
    public WorkOrder handle(CancelWorkOrderCommand command) {
        WorkOrder order = findOrder(command.tenantId(), command.workOrderId());
        order.assertExpectedVersion(command.expectedVersion());
        order.cancel(command.reason(), command.actorId(), clock.instant());
        WorkOrder saved = workOrderRepository.save(order);

        // The incident is dismissed: the alert is discarded with the same reason.
        Alert alert = findAlert(command.tenantId(), order.getAlertId());
        alert.dismissByCancelledOrder(command.reason());
        alertRepository.save(alert);
        return saved;
    }

    @Override
    @Transactional
    public EvidencePhoto handle(AttachEvidenceCommand command) {
        WorkOrder order = findOrder(command.tenantId(), command.workOrderId());
        requireAssignedTo(order, command.actorId());
        if (order.getStatus() != WorkOrderStatus.IN_PROGRESS) {
            throw new ApplicationException(MaintenanceError.WORK_ORDER_NOT_IN_PROGRESS);
        }
        byte[] content = command.content();
        if (content != null && content.length > maxEvidenceBytes) {
            throw new ApplicationException(CommonError.PAYLOAD_TOO_LARGE);
        }
        ImageFormat format = ImageFormat.detect(content)
                .orElseThrow(() -> new ApplicationException(MaintenanceError.UNSUPPORTED_IMAGE));

        String storageKey = evidenceStorage.store(command.tenantId(), command.workOrderId(),
                format.extension(), content);
        try {
            return evidenceRepository.save(EvidencePhoto.create(command.tenantId(), command.workOrderId(),
                    storageKey, format.mimeType(), clock.instant()));
        } catch (RuntimeException ex) {
            evidenceStorage.delete(storageKey); // do not leave a file nobody points to
            throw ex;
        }
    }

    private WorkOrder findOrder(UUID tenantId, UUID workOrderId) {
        return workOrderRepository.findByIdAndTenantId(workOrderId, tenantId)
                .orElseThrow(() -> new ApplicationException(MaintenanceError.WORK_ORDER_NOT_FOUND));
    }

    private Alert findAlert(UUID tenantId, UUID alertId) {
        return alertRepository.findByIdAndTenantId(alertId, tenantId)
                .orElseThrow(() -> new ApplicationException(MaintenanceError.ALERT_NOT_FOUND));
    }

    /** The technician only modifies his own orders. */
    private static void requireAssignedTo(WorkOrder order, UUID actorId) {
        if (!order.isAssignedTo(actorId)) {
            throw new ApplicationException(MaintenanceError.NOT_ASSIGNED_TECHNICIAN);
        }
    }
}