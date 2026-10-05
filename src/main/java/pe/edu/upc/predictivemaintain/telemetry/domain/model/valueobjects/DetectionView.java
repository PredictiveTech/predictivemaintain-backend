package pe.edu.upc.predictivemaintain.telemetry.domain.model.valueobjects;

import pe.edu.upc.predictivemaintain.telemetry.domain.model.aggregates.AnomalyDetection;
import pe.edu.upc.predictivemaintain.telemetry.domain.model.aggregates.SensorReading;

/**
 * A detection together with the reading that originated it (read model for the analytics screen).
 */
public record DetectionView(AnomalyDetection detection, SensorReading reading) {
}