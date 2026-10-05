package pe.edu.upc.predictivemaintain.telemetry.interfaces.rest.resources;

import pe.edu.upc.predictivemaintain.telemetry.application.queryservices.SensorPanelItem.CommunicationStatus;
import pe.edu.upc.predictivemaintain.telemetry.application.queryservices.SensorPanelItem.RangeStatus;
import pe.edu.upc.predictivemaintain.telemetry.domain.model.valueobjects.Metric;
import pe.edu.upc.predictivemaintain.telemetry.domain.model.valueobjects.ThresholdSeverity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * One variable of an asset, as shown on its screen.
 *
 * @param lastReceivedAt time of the last reading received; null if the sensor never sent one
 * @param latestReading  null if the sensor never sent a reading
 * @param threshold      null if no threshold is configured
 */
public record SensorPanelItemResource(UUID id, Metric metric, String unit, boolean active,
                                      CommunicationStatus communication, Instant lastReceivedAt,
                                      LatestReadingResource latestReading, ThresholdInfoResource threshold,
                                      RangeStatus rangeStatus) {

    public record LatestReadingResource(BigDecimal value, Instant measuredAt) {
    }

    public record ThresholdInfoResource(BigDecimal lowerBound, BigDecimal upperBound,
                                        ThresholdSeverity severity, int version) {
    }
}