package pe.edu.upc.predictivemaintain.maintenance.application.commandservices;

import pe.edu.upc.predictivemaintain.maintenance.domain.model.aggregates.Alert;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.commands.ConfirmAlertCommand;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.commands.DiscardAlertCommand;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.commands.RegisterAlertCommand;

/**
 * Write use cases for alerts.
 */
public interface AlertCommandService {

    /** Creates the alert for an anomaly event. Idempotent by sourceEventId. */
    Alert handle(RegisterAlertCommand command);

    Alert handle(ConfirmAlertCommand command);

    Alert handle(DiscardAlertCommand command);
}