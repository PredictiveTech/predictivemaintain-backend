package pe.edu.upc.predictivemaintain.maintenance.infrastructure.weather;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import pe.edu.upc.predictivemaintain.maintenance.application.outboundservices.WeatherObservation;
import pe.edu.upc.predictivemaintain.maintenance.application.outboundservices.WeatherProvider;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

/**
 * Current weather from Open-Meteo (https://open-meteo.com): free and without API key.
 * Any failure (timeout, network, unexpected response) becomes an empty result.
 */
@Component
public class OpenMeteoWeatherAdapter implements WeatherProvider {

    private static final Logger log = LoggerFactory.getLogger(OpenMeteoWeatherAdapter.class);

    private final RestClient restClient;

    public OpenMeteoWeatherAdapter(RestClient.Builder restClientBuilder,
                                   @Value("${app.weather.base-url}") String baseUrl,
                                   @Value("${app.weather.timeout-seconds:3}") long timeoutSeconds) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(timeoutSeconds));
        requestFactory.setReadTimeout(Duration.ofSeconds(timeoutSeconds));
        this.restClient = restClientBuilder.baseUrl(baseUrl).requestFactory(requestFactory).build();
    }

    @Override
    public Optional<WeatherObservation> currentWeather(double latitude, double longitude) {
        try {
            OpenMeteoResponse response = restClient.get()
                    .uri(uri -> uri.path("/v1/forecast")
                            .queryParam("latitude", latitude)
                            .queryParam("longitude", longitude)
                            .queryParam("current", "temperature_2m,relative_humidity_2m")
                            .queryParam("timezone", "UTC")
                            .build())
                    .retrieve()
                    .body(OpenMeteoResponse.class);

            if (response == null || response.current() == null
                    || response.current().time() == null
                    || response.current().temperature() == null
                    || response.current().humidity() == null) {
                return Optional.empty();
            }
            Instant observedAt = LocalDateTime.parse(response.current().time()).toInstant(ZoneOffset.UTC);
            return Optional.of(new WeatherObservation(
                    response.current().temperature(), response.current().humidity(), observedAt));
        } catch (RuntimeException ex) {
            log.warn("Weather service unavailable: {}", ex.getMessage());
            return Optional.empty();
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record OpenMeteoResponse(Current current) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Current(String time,
                   @JsonProperty("temperature_2m") Double temperature,
                   @JsonProperty("relative_humidity_2m") Double humidity) {
    }
}