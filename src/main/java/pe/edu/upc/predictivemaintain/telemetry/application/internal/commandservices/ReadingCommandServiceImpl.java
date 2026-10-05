package pe.edu.upc.predictivemaintain.telemetry.application.internal.commandservices;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.predictivemaintain.maintenance.interfaces.acl.MaintenanceContextFacade;
import pe.edu.upc.predictivemaintain.shared.application.errors.ApplicationException;
import pe.edu.upc.predictivemaintain.subscription.interfaces.acl.SubscriptionContextFacade; // NEW
import pe.edu.upc.predictivemaintain.telemetry.application.commandservices.IngestReadingResult;
import pe.edu.upc.predictivemaintain.telemetry.application.commandservices.ReadingCommandService;
import pe.edu.upc.predictivemaintain.telemetry.application.errors.TelemetryError;
import pe.edu.upc.predictivemaintain.telemetry.application.internal.support.DeviceKeySupport;
import pe.edu.upc.predictivemaintain.telemetry.domain.model.aggregates.AnomalyDetection;
import pe.edu.upc.predictivemaintain.telemetry.domain.model.aggregates.Sensor;
import pe.edu.upc.predictivemaintain.telemetry.domain.model.aggregates.SensorReading;
import pe.edu.upc.predictivemaintain.telemetry.domain.model.aggregates.ThresholdRule;
import pe.edu.upc.predictivemaintain.telemetry.domain.model.commands.IngestReadingCommand;
import pe.edu.upc.predictivemaintain.telemetry.domain.model.valueobjects.Measurement;
import pe.edu.upc.predictivemaintain.telemetry.domain.repositories.AnomalyDetectionRepository;
import pe.edu.upc.predictivemaintain.telemetry.domain.repositories.SensorReadingRepository;
import pe.edu.upc.predictivemaintain.telemetry.domain.repositories.SensorRepository;
import pe.edu.upc.predictivemaintain.telemetry.domain.repositories.ThresholdRuleRepository;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class ReadingCommandServiceImpl implements ReadingCommandService {

    private final SensorRepository sensorRepository;
    private final SensorReadingRepository readingRepository;
    private final ThresholdRuleRepository thresholdRuleRepository;
    private final AnomalyDetectionRepository detectionRepository;
    private final MaintenanceContextFacade maintenanceContextFacade;
    private final SubscriptionContextFacade subscriptionContextFacade; // NEW
    private final Clock clock;
    private final Duration minInterval;
    private final Duration alertCooldown;

    public ReadingCommandServiceImpl(SensorRepository sensorRepository,
                                     SensorReadingRepository readingRepository,
                                     ThresholdRuleRepository thresholdRuleRepository,
                                     AnomalyDetectionRepository detectionRepository,
                                     MaintenanceContextFacade maintenanceContextFacade,
                                     SubscriptionContextFacade subscriptionContextFacade, // NEW
                                     Clock clock,
                                     @Value("${app.telemetry.min-interval-seconds:1}") long minIntervalSeconds,
                                     @Value("${app.telemetry.alert-cooldown-seconds:600}") long alertCooldownSeconds) {
        this.sensorRepository = sensorRepository;
        this.readingRepository = readingRepository;
        this.thresholdRuleRepository = thresholdRuleRepository;
        this.detectionRepository = detectionRepository;
        this.maintenanceContextFacade = maintenanceContextFacade;
        this.subscriptionContextFacade = subscriptionContextFacade; // NEW
        this.clock = clock;
        this.minInterval = Duration.ofSeconds(minIntervalSeconds);
        this.alertCooldown = Duration.ofSeconds(alertCooldownSeconds);
    }

    @Override
    @Transactional
    public IngestReadingResult handle(IngestReadingCommand command) {
        Sensor sensor = authenticate(command);
        UUID tenantId = sensor.getTenantId();

        // Idempotent: a repeated sourceKey returns the stored reading and creates nothing new.
        Optional<SensorReading> existing =
                readingRepository.findBySensorIdAndSourceKey(tenantId, sensor.getId(), command.sourceKey());
        if (existing.isPresent()) {
            AnomalyDetection previous = detectionRepository.findByReadingId(tenantId, existing.get().getId())
                    .orElse(null);
            return new IngestReadingResult(existing.get(), false, previous);
        }

        // US-19, scenario 2: without a valid subscription the platform stops accepting monitoring data.
        subscriptionContextFacade.requireMonitoringAllowed(tenantId); // NEW

        if (!sensor.isActive()) {
            throw new ApplicationException(TelemetryError.SENSOR_INACTIVE);
        }
        if (!sensor.acceptsUnit(command.unit())) {
            throw new ApplicationException(TelemetryError.UNIT_MISMATCH, command.unit(), sensor.getUnit());
        }
        // US-13: when the asset is deactivated, its sensors stop being accepted.
        if (maintenanceContextFacade.assetState(tenantId, sensor.getAssetId())
                != MaintenanceContextFacade.AssetState.ACTIVE) {
            throw new ApplicationException(TelemetryError.ASSET_INACTIVE);
        }

        Instant now = clock.instant();
        // TS-01, scenario 4: readings that arrive faster than the minimum interval are refused.
        if (sensor.getLastReceivedAt() != null && now.isBefore(sensor.getLastReceivedAt().plus(minInterval))) {
            throw new ApplicationException(TelemetryError.READING_TOO_FREQUENT);
        }

        Measurement measurement = new Measurement(command.value(), command.unit());
        SensorReading reading = readingRepository.save(
                SensorReading.create(sensor, command.sourceKey(), measurement, command.measuredAt(), now));
        sensor.recordReception(now);

        AnomalyDetection detection = evaluate(sensor, reading, now);
        sensorRepository.save(sensor);
        return new IngestReadingResult(reading, true, detection);
    }

    /** Compares the reading with the sensor's rule; if it is outside, records the detection and may raise an alert. */
    private AnomalyDetection evaluate(Sensor sensor, SensorReading reading, Instant now) {
        Optional<ThresholdRule> rule = thresholdRuleRepository.findBySensorId(sensor.getTenantId(), sensor.getId());
        if (rule.isEmpty() || !rule.get().isOutOfRange(reading.getValue())) {
            return null;
        }

        AnomalyDetection detection = AnomalyDetection.fromThreshold(reading, rule.get(), now);
        if (sensor.canRaiseAlert(now, alertCooldown)) {
            maintenanceContextFacade.registerAlert(
                    sensor.getTenantId(),
                    sensor.getAssetId(),
                    detection.getId(),
                    rule.get().getSeverity().name(),
                    new MaintenanceContextFacade.AlertEvidence(sensor.getMetric().name(), sensor.getUnit(),
                            reading.getValue(), rule.get().getLowerBound(), rule.get().getUpperBound(),
                            reading.getMeasuredAt()));
            detection.markAlertRaised();
            sensor.recordAlert(now);
        }
        return detectionRepository.save(detection);
    }

    /**
     * Unknown sensor -> 404 (TS-01). Known sensor with a missing or wrong key -> 401.
     * The key is compared by hash and in constant time.
     */
    private Sensor authenticate(IngestReadingCommand command) {
        Sensor sensor = sensorRepository.findById(command.sensorId())
                .orElseThrow(() -> new ApplicationException(TelemetryError.SENSOR_NOT_FOUND));
        String candidateHash = DeviceKeySupport.hash(command.deviceKey() == null ? "" : command.deviceKey());
        if (!sensor.matchesDeviceKeyHash(candidateHash)) {
            throw new ApplicationException(TelemetryError.INVALID_DEVICE_CREDENTIAL);
        }
        return sensor;
    }
}