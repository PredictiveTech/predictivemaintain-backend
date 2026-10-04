package pe.edu.upc.predictivemaintain.telemetry.interfaces.rest.transform;

import pe.edu.upc.predictivemaintain.telemetry.application.commandservices.RegisteredSensor;
import pe.edu.upc.predictivemaintain.telemetry.domain.model.aggregates.Sensor;
import pe.edu.upc.predictivemaintain.telemetry.domain.model.aggregates.ThresholdRule;
import pe.edu.upc.predictivemaintain.telemetry.interfaces.rest.resources.SensorRegisteredResource;
import pe.edu.upc.predictivemaintain.telemetry.interfaces.rest.resources.ThresholdResource;

public final class SensorResourceFromEntityAssembler {

    private SensorResourceFromEntityAssembler() {
    }

    public static SensorRegisteredResource toResource(RegisteredSensor registered) {
        Sensor sensor = registered.sensor();
        return new SensorRegisteredResource(sensor.getId(), sensor.getAssetId(), sensor.getMetric(),
                sensor.getUnit(), sensor.isActive(), registered.deviceKey());
    }

    public static ThresholdResource toResource(ThresholdRule rule) {
        return new ThresholdResource(rule.getSensorId(), rule.getLowerBound(), rule.getUpperBound(),
                rule.getSeverity(), rule.getVersion());
    }
}