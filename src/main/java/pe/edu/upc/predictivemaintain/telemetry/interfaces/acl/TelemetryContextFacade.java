package pe.edu.upc.predictivemaintain.telemetry.interfaces.acl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pe.edu.upc.predictivemaintain.telemetry.domain.model.aggregates.Sensor;
import pe.edu.upc.predictivemaintain.telemetry.domain.repositories.SensorRepository;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Anti-Corruption Layer: what the Telemetry context exposes to other bounded contexts.
 */
@Service
public class TelemetryContextFacade {

    /**
     * @param hasSensors    the asset has at least one sensor
     * @param communicating at least one active sensor sent a reading within the communication timeout
     * @param metrics       the physical variables the asset's sensors measure (names, sorted)
     */
    public record AssetTelemetry(boolean hasSensors, boolean communicating, Set<String> metrics) {
    }

    private final SensorRepository sensorRepository;
    private final Clock clock;
    private final Duration communicationTimeout;

    public TelemetryContextFacade(SensorRepository sensorRepository, Clock clock,
                                  @Value("${app.telemetry.communication-timeout-seconds:300}") long timeoutSeconds) {
        this.sensorRepository = sensorRepository;
        this.clock = clock;
        this.communicationTimeout = Duration.ofSeconds(timeoutSeconds);
    }

    /** One entry for every requested asset, including those without sensors. */
    public Map<UUID, AssetTelemetry> summarize(UUID tenantId, Collection<UUID> assetIds) {
        if (assetIds.isEmpty()) {
            return Map.of();
        }
        Instant now = clock.instant();
        Map<UUID, List<Sensor>> sensorsByAsset = sensorRepository.findByAssetIds(tenantId, assetIds).stream()
                .collect(Collectors.groupingBy(Sensor::getAssetId));

        Map<UUID, AssetTelemetry> summary = new HashMap<>();
        for (UUID assetId : assetIds) {
            List<Sensor> sensors = sensorsByAsset.getOrDefault(assetId, List.of());
            boolean communicating = sensors.stream()
                    .anyMatch(sensor -> sensor.isActive() && sensor.isCommunicating(now, communicationTimeout));
            Set<String> metrics = sensors.stream()
                    .map(sensor -> sensor.getMetric().name())
                    .collect(Collectors.toCollection(TreeSet::new));
            summary.put(assetId, new AssetTelemetry(!sensors.isEmpty(), communicating, metrics));
        }
        return summary;
    }
}