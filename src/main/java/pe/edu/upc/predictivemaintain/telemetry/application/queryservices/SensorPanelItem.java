package pe.edu.upc.predictivemaintain.telemetry.application.queryservices;

import pe.edu.upc.predictivemaintain.telemetry.domain.model.aggregates.Sensor;
import pe.edu.upc.predictivemaintain.telemetry.domain.model.aggregates.SensorReading;
import pe.edu.upc.predictivemaintain.telemetry.domain.model.aggregates.ThresholdRule;

/**
 * Everything the screen of an asset needs to show about one sensor.
 *
 * @param latestReading the most recent reading, or null if the sensor never sent one
 * @param threshold     the sensor's rule, or null if the manager has not configured it
 */
public record SensorPanelItem(Sensor sensor, CommunicationStatus communication, SensorReading latestReading,
                              ThresholdRule threshold, RangeStatus rangeStatus) {

    /** NO_COMMUNICATION: no reading for longer than the configured timeout (5 minutes by default), or never. */
    public enum CommunicationStatus {
        ONLINE,
        NO_COMMUNICATION
    }

    /** UNKNOWN: there is no reading or no threshold to compare. */
    public enum RangeStatus {
        NORMAL,
        OUT_OF_RANGE,
        UNKNOWN
    }
}