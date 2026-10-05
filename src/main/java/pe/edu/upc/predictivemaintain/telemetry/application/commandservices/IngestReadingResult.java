package pe.edu.upc.predictivemaintain.telemetry.application.commandservices;

import pe.edu.upc.predictivemaintain.telemetry.domain.model.aggregates.AnomalyDetection;
import pe.edu.upc.predictivemaintain.telemetry.domain.model.aggregates.SensorReading;

/**
 * @param created   false when the reading was already stored (the device repeated its sourceKey)
 * @param detection the anomaly found in the reading, or null if it was within the allowed range
 */
public record IngestReadingResult(SensorReading reading, boolean created, AnomalyDetection detection) {
}