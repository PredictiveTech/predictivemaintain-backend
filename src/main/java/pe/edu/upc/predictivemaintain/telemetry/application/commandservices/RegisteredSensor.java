package pe.edu.upc.predictivemaintain.telemetry.application.commandservices;

import pe.edu.upc.predictivemaintain.telemetry.domain.model.aggregates.Sensor;

/**
 * @param deviceKey the plain key; it exists only in this answer and cannot be recovered later
 */
public record RegisteredSensor(Sensor sensor, String deviceKey) {
}