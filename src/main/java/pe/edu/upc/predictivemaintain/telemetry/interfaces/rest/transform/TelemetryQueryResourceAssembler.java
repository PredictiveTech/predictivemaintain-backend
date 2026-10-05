package pe.edu.upc.predictivemaintain.telemetry.interfaces.rest.transform;

import pe.edu.upc.predictivemaintain.telemetry.application.queryservices.SensorPanelItem;
import pe.edu.upc.predictivemaintain.telemetry.domain.model.aggregates.Sensor;
import pe.edu.upc.predictivemaintain.telemetry.domain.model.aggregates.SensorReading;
import pe.edu.upc.predictivemaintain.telemetry.domain.model.aggregates.ThresholdRule;
import pe.edu.upc.predictivemaintain.telemetry.interfaces.rest.resources.ReadingResource;
import pe.edu.upc.predictivemaintain.telemetry.interfaces.rest.resources.SensorPanelItemResource;
import pe.edu.upc.predictivemaintain.telemetry.interfaces.rest.resources.SensorPanelItemResource.LatestReadingResource;
import pe.edu.upc.predictivemaintain.telemetry.interfaces.rest.resources.SensorPanelItemResource.ThresholdInfoResource;

import java.util.Map;
import java.util.UUID;

public final class TelemetryQueryResourceAssembler {

    private TelemetryQueryResourceAssembler() {
    }

    public static SensorPanelItemResource toResource(SensorPanelItem item) {
        Sensor sensor = item.sensor();
        SensorReading latest = item.latestReading();
        ThresholdRule rule = item.threshold();
        return new SensorPanelItemResource(
                sensor.getId(),
                sensor.getMetric(),
                sensor.getUnit(),
                sensor.isActive(),
                item.communication(),
                sensor.getLastReceivedAt(),
                latest == null ? null : new LatestReadingResource(latest.getValue(), latest.getMeasuredAt()),
                rule == null ? null : new ThresholdInfoResource(rule.getLowerBound(), rule.getUpperBound(),
                        rule.getSeverity(), rule.getVersion()),
                item.rangeStatus());
    }

    public static ReadingResource toResource(SensorReading reading, Map<UUID, Sensor> sensors) {
        Sensor sensor = sensors.get(reading.getSensorId());
        return new ReadingResource(reading.getId(), reading.getSensorId(),
                sensor == null ? null : sensor.getMetric(), reading.getUnit(), reading.getValue(),
                reading.getMeasuredAt(), reading.getReceivedAt());
    }
}