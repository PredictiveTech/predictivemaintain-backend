package pe.edu.upc.predictivemaintain.telemetry.application.internal.queryservices;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.predictivemaintain.maintenance.interfaces.acl.MaintenanceContextFacade;
import pe.edu.upc.predictivemaintain.shared.application.errors.ApplicationException;
import pe.edu.upc.predictivemaintain.shared.domain.exceptions.DomainValidationException;
import pe.edu.upc.predictivemaintain.shared.domain.model.valueobjects.PagedResult;
import pe.edu.upc.predictivemaintain.telemetry.application.errors.TelemetryError;
import pe.edu.upc.predictivemaintain.telemetry.application.queryservices.AssetReadings;
import pe.edu.upc.predictivemaintain.telemetry.application.queryservices.SensorPanelItem;
import pe.edu.upc.predictivemaintain.telemetry.application.queryservices.SensorPanelItem.CommunicationStatus;
import pe.edu.upc.predictivemaintain.telemetry.application.queryservices.SensorPanelItem.RangeStatus;
import pe.edu.upc.predictivemaintain.telemetry.application.queryservices.TelemetryQueryService;
import pe.edu.upc.predictivemaintain.telemetry.domain.model.aggregates.Sensor;
import pe.edu.upc.predictivemaintain.telemetry.domain.model.aggregates.SensorReading;
import pe.edu.upc.predictivemaintain.telemetry.domain.model.aggregates.ThresholdRule;
import pe.edu.upc.predictivemaintain.telemetry.domain.model.queries.GetAssetReadingsQuery;
import pe.edu.upc.predictivemaintain.telemetry.domain.model.queries.GetAssetSensorsQuery;
import pe.edu.upc.predictivemaintain.telemetry.domain.repositories.SensorReadingRepository;
import pe.edu.upc.predictivemaintain.telemetry.domain.repositories.SensorRepository;
import pe.edu.upc.predictivemaintain.telemetry.domain.repositories.ThresholdRuleRepository;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class TelemetryQueryServiceImpl implements TelemetryQueryService {

    private static final Duration DEFAULT_RANGE = Duration.ofHours(24);
    private static final Duration MAX_RANGE = Duration.ofDays(31);

    private final SensorRepository sensorRepository;
    private final SensorReadingRepository readingRepository;
    private final ThresholdRuleRepository thresholdRuleRepository;
    private final MaintenanceContextFacade maintenanceContextFacade;
    private final Clock clock;
    private final Duration communicationTimeout;

    public TelemetryQueryServiceImpl(SensorRepository sensorRepository,
                                     SensorReadingRepository readingRepository,
                                     ThresholdRuleRepository thresholdRuleRepository,
                                     MaintenanceContextFacade maintenanceContextFacade,
                                     Clock clock,
                                     @Value("${app.telemetry.communication-timeout-seconds:300}") long timeoutSeconds) {
        this.sensorRepository = sensorRepository;
        this.readingRepository = readingRepository;
        this.thresholdRuleRepository = thresholdRuleRepository;
        this.maintenanceContextFacade = maintenanceContextFacade;
        this.clock = clock;
        this.communicationTimeout = Duration.ofSeconds(timeoutSeconds);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SensorPanelItem> handle(GetAssetSensorsQuery query) {
        requireAssetExists(query.tenantId(), query.assetId());
        List<Sensor> sensors = sensorRepository.findByAssetId(query.tenantId(), query.assetId());

        Map<UUID, ThresholdRule> rules = thresholdRuleRepository
                .findBySensorIds(query.tenantId(), sensors.stream().map(Sensor::getId).toList()).stream()
                .collect(Collectors.toMap(ThresholdRule::getSensorId, Function.identity()));

        Instant now = clock.instant();
        // One query per sensor for its latest reading: fine for the few sensors one asset has.
        return sensors.stream()
                .map(sensor -> buildItem(sensor, rules.get(sensor.getId()), now))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AssetReadings handle(GetAssetReadingsQuery query) {
        requireAssetExists(query.tenantId(), query.assetId());

        Instant to = query.to() != null ? query.to() : clock.instant();
        Instant from = query.from() != null ? query.from() : to.minus(DEFAULT_RANGE);
        if (from.isAfter(to)) {
            throw new DomainValidationException("validation.reading.range-invalid");
        }
        if (Duration.between(from, to).compareTo(MAX_RANGE) > 0) {
            throw new DomainValidationException("validation.reading.range-too-large");
        }

        Map<UUID, Sensor> sensors = sensorRepository.findByAssetId(query.tenantId(), query.assetId()).stream()
                .collect(Collectors.toMap(Sensor::getId, Function.identity()));
        if (query.sensorId() != null) {
            Sensor selected = sensors.get(query.sensorId());
            if (selected == null) {
                throw new ApplicationException(TelemetryError.SENSOR_NOT_FOUND);
            }
            sensors = Map.of(selected.getId(), selected);
        }

        PagedResult<SensorReading> page =
                readingRepository.search(query.tenantId(), sensors.keySet(), from, to, query.page());
        return new AssetReadings(page, sensors);
    }

    private SensorPanelItem buildItem(Sensor sensor, ThresholdRule rule, Instant now) {
        SensorReading latest = readingRepository.findLatestBySensorId(sensor.getTenantId(), sensor.getId())
                .orElse(null);
        CommunicationStatus communication = sensor.isCommunicating(now, communicationTimeout)
                ? CommunicationStatus.ONLINE
                : CommunicationStatus.NO_COMMUNICATION;
        RangeStatus range;
        if (latest == null || rule == null) {
            range = RangeStatus.UNKNOWN;
        } else {
            range = rule.isOutOfRange(latest.getValue()) ? RangeStatus.OUT_OF_RANGE : RangeStatus.NORMAL;
        }
        return new SensorPanelItem(sensor, communication, latest, rule, range);
    }

    /** Inactive assets can still be consulted (their history matters); only unknown ones are an error. */
    private void requireAssetExists(UUID tenantId, UUID assetId) {
        if (maintenanceContextFacade.assetState(tenantId, assetId) == MaintenanceContextFacade.AssetState.NOT_FOUND) {
            throw new ApplicationException(TelemetryError.ASSET_NOT_FOUND);
        }
    }
}