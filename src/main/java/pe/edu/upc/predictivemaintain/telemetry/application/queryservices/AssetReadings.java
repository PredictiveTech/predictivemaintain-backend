package pe.edu.upc.predictivemaintain.telemetry.application.queryservices;

import pe.edu.upc.predictivemaintain.shared.domain.model.valueobjects.PagedResult;
import pe.edu.upc.predictivemaintain.telemetry.domain.model.aggregates.Sensor;
import pe.edu.upc.predictivemaintain.telemetry.domain.model.aggregates.SensorReading;

import java.util.Map;
import java.util.UUID;

/**
 * One page of readings plus the sensors they belong to (so each reading can show its variable and unit).
 */
public record AssetReadings(PagedResult<SensorReading> page, Map<UUID, Sensor> sensors) {
}