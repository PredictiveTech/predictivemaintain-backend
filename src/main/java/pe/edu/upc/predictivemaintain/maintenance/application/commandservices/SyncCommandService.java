package pe.edu.upc.predictivemaintain.maintenance.application.commandservices;

import pe.edu.upc.predictivemaintain.maintenance.domain.model.commands.SyncWorkOrdersCommand;

/**
 * Receives what a technician did without a connection (US-28).
 */
public interface SyncCommandService {

    SyncResult handle(SyncWorkOrdersCommand command);
}