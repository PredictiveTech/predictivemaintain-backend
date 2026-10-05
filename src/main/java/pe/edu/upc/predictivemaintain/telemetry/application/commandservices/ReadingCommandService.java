package pe.edu.upc.predictivemaintain.telemetry.application.commandservices;

import pe.edu.upc.predictivemaintain.telemetry.domain.model.commands.IngestReadingCommand;

/**
 * Receives the readings sent by the sensors.
 */
public interface ReadingCommandService {

    IngestReadingResult handle(IngestReadingCommand command);
}