package pe.edu.upc.predictivemaintain.telemetry.application.commandservices;

import pe.edu.upc.predictivemaintain.telemetry.domain.model.aggregates.ThresholdRule;
import pe.edu.upc.predictivemaintain.telemetry.domain.model.commands.ConfigureThresholdCommand;
import pe.edu.upc.predictivemaintain.telemetry.domain.model.commands.RegisterSensorCommand;

/**
 * Write use cases for sensors and their threshold rules.
 */
public interface SensorCommandService {

    RegisteredSensor handle(RegisterSensorCommand command);

    /** Creates the rule of the sensor, or changes it (and increases its version) if it already exists. */
    ThresholdRule handle(ConfigureThresholdCommand command);
}