package pe.edu.upc.predictivemaintain.maintenance.interfaces.rest.transform;

import pe.edu.upc.predictivemaintain.maintenance.application.outboundservices.WeatherObservation;
import pe.edu.upc.predictivemaintain.maintenance.application.queryservices.AssetWeatherResult;
import pe.edu.upc.predictivemaintain.maintenance.interfaces.rest.resources.WeatherResource;

public final class WeatherResourceFromResultAssembler {

    private WeatherResourceFromResultAssembler() {
    }

    public static WeatherResource toResource(AssetWeatherResult result) {
        WeatherObservation observation = result.observation();
        return new WeatherResource(
                observation == null ? null : observation.temperatureCelsius(),
                observation == null ? null : observation.relativeHumidityPercent(),
                observation == null ? null : observation.observedAt(),
                result.availability().name());
    }
}