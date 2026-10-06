package pe.edu.upc.predictivemaintain.maintenance.interfaces.rest.transform;

import pe.edu.upc.predictivemaintain.maintenance.application.queryservices.AlertLabel;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.aggregates.EvidencePhoto;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.aggregates.WorkOrder;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.aggregates.WorkOrderChange;
import pe.edu.upc.predictivemaintain.maintenance.interfaces.rest.resources.EvidenceResource;
import pe.edu.upc.predictivemaintain.maintenance.interfaces.rest.resources.WorkOrderChangeResource;
import pe.edu.upc.predictivemaintain.maintenance.interfaces.rest.resources.WorkOrderResource;

public final class WorkOrderResourceFromEntityAssembler {

    private WorkOrderResourceFromEntityAssembler() {
    }

    /** @param alert what the order's alert is about (asset and severity); null leaves those fields empty */
    public static WorkOrderResource toResource(WorkOrder order, AlertLabel alert) {
        return new WorkOrderResource(order.getId(), order.getAlertId(),
                alert == null ? null : alert.assetId(),
                alert == null ? null : alert.assetCode(),
                alert == null ? null : alert.assetName(),
                alert == null ? null : alert.severity(),
                order.getAssignedUserId(), order.getStatus(), order.getSummary(), order.getOpenedAt(),
                order.getCompletedAt(), order.getVersion());
    }

    public static WorkOrderChangeResource toResource(WorkOrderChange change) {
        return new WorkOrderChangeResource(change.id(), change.actorId(), change.previousStatus(),
                change.newStatus(), change.previousAssigneeId(), change.newAssigneeId(), change.changedAt());
    }

    public static EvidenceResource toResource(EvidencePhoto photo) {
        return new EvidenceResource(photo.id(), photo.workOrderId(), photo.mimeType(), photo.uploadedAt());
    }
}