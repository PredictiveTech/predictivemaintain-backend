package pe.edu.upc.predictivemaintain.telemetry.interfaces.rest.transform;

import pe.edu.upc.predictivemaintain.shared.interfaces.rest.resources.PagedResource;
import pe.edu.upc.predictivemaintain.telemetry.application.queryservices.AssetAnalytics;
import pe.edu.upc.predictivemaintain.telemetry.application.queryservices.RulResult;
import pe.edu.upc.predictivemaintain.telemetry.domain.model.aggregates.AnomalyDetection;
import pe.edu.upc.predictivemaintain.telemetry.domain.model.aggregates.Sensor;
import pe.edu.upc.predictivemaintain.telemetry.domain.model.aggregates.SensorReading;
import pe.edu.upc.predictivemaintain.telemetry.domain.model.valueobjects.DetectionView;
import pe.edu.upc.predictivemaintain.telemetry.interfaces.rest.resources.AnalyticsResource;
import pe.edu.upc.predictivemaintain.telemetry.interfaces.rest.resources.DetectionResource;
import pe.edu.upc.predictivemaintain.telemetry.interfaces.rest.resources.RulResource;

import java.util.Map;
import java.util.UUID;

public final class AnalyticsResourceAssembler {

    private AnalyticsResourceAssembler() {
    }

    public static RulResource toResource(RulResult rul) {
        return new RulResource(rul.availability(), rul.hoursRemaining(), rul.confidence(), rul.modelVersion(),
                rul.estimatedAt(), rul.drivingMetric(), rul.interventionRecommended());
    }

    public static DetectionResource toResource(DetectionView view, Map<UUID, Sensor> sensors) {
        AnomalyDetection detection = view.detection();
        SensorReading reading = view.reading();
        Sensor sensor = sensors.get(reading.getSensorId());
        return new DetectionResource(detection.getId(), reading.getSensorId(),
                sensor == null ? null : sensor.getMetric(), reading.getUnit(), reading.getValue(),
                detection.getSeverity(), detection.getLowerSnapshot(), detection.getUpperSnapshot(),
                detection.getRuleVersion(), detection.isAlertRaised(), detection.getDetectedAt(),
                reading.getMeasuredAt());
    }

    public static AnalyticsResource toResource(AssetAnalytics analytics) {
        return new AnalyticsResource(
                PagedResource.from(analytics.detections(), view -> toResource(view, analytics.sensors())),
                toResource(analytics.rul()));
    }
}