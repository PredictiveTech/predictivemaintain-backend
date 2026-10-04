package pe.edu.upc.predictivemaintain.maintenance.application.commandservices;

import pe.edu.upc.predictivemaintain.maintenance.domain.model.aggregates.DowntimeInterval;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.aggregates.ProductionWindow;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.commands.RecordDowntimeCommand;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.commands.RecordProductionWindowCommand;

/**
 * Records the operation data that feeds the availability and OEE reports.
 */
public interface OperationDataCommandService {

    DowntimeInterval handle(RecordDowntimeCommand command);

    ProductionWindow handle(RecordProductionWindowCommand command);
}