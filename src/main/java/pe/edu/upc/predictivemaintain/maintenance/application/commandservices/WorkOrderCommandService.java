package pe.edu.upc.predictivemaintain.maintenance.application.commandservices;

import pe.edu.upc.predictivemaintain.maintenance.domain.model.aggregates.EvidencePhoto;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.aggregates.WorkOrder;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.commands.AssignWorkOrderCommand;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.commands.AttachEvidenceCommand;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.commands.CancelWorkOrderCommand;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.commands.CompleteWorkOrderCommand;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.commands.CreateWorkOrderCommand;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.commands.StartWorkOrderCommand;

/**
 * Write use cases for work orders and their evidence.
 */
public interface WorkOrderCommandService {

    WorkOrder handle(CreateWorkOrderCommand command);

    WorkOrder handle(AssignWorkOrderCommand command);

    WorkOrder handle(StartWorkOrderCommand command);

    WorkOrder handle(CompleteWorkOrderCommand command);

    WorkOrder handle(CancelWorkOrderCommand command);

    EvidencePhoto handle(AttachEvidenceCommand command);
}