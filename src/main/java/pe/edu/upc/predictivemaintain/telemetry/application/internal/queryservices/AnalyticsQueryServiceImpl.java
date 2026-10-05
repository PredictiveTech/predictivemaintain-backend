package pe.edu.upc.predictivemaintain.telemetry.application.internal.queryservices;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.predictivemaintain.maintenance.interfaces.acl.MaintenanceContextFacade;
import pe.edu.upc.predictivemaintain.shared.application.errors.ApplicationException;
import pe.edu.upc.predictivemaintain.shared.domain.exceptions.DomainValidationException;
import pe.edu.upc.predictivemaintain.shared.domain.model.valueobjects.PageQuery;
import pe.edu.upc.predictivemaintain.shared.domain.model.valueobjects.PagedResult;
import pe.edu.upc.predictivemaintain.telemetry.application.errors.TelemetryError;
import pe.edu.upc.predictivemaintain.telemetry.application.outboundservices.PredictiveModel;
import pe.edu.upc.predictivemaintain.telemetry.application.outboundservices.ReadingPoint;
import pe.edu.upc.predictivemaintain.telemetry.application.outboundservices.RulPrediction;
import pe.edu.upc.predictivemaintain.telemetry.application.queryservices.AnalyticsQueryService;
import pe.edu.upc.predictivemaintain.telemetry.application.queryservices.AssetAnalytics;
import pe.edu.upc.predictivemaintain.telemetry.application.queryservices.RulResult;
import pe.edu.upc.predictivemaintain.telemetry.domain.model.aggregates.Sensor;
import pe.edu.upc.predictivemaintain.telemetry.domain.model.aggregates.SensorReading;
import pe.edu.upc.predictivemaintain.telemetry.domain.model.aggregates.ThresholdRule;
import pe.edu.upc.predictivemaintain.telemetry.domain.model.queries.GetAssetAnalyticsQuery;
import pe.edu.upc.predictivemaintain.telemetry.domain.model.queries.GetAssetRulQuery;
import pe.edu.upc.predictivemaintain.telemetry.domain.model.valueobjects.DetectionView;
import pe.edu.upc.predictivemaintain.telemetry.domain.repositories.SensorReadingRepository;
import pe.edu.upc.predictivemaintain.telemetry.domain.repositories.SensorRepository;
import pe.edu.upc.predictivemaintain.telemetry.domain.repositories.TelemetryAnalyticsRepository;
import pe.edu.upc.predictivemaintain.telemetry.domain.repositories.ThresholdRuleRepository;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AnalyticsQueryServiceImpl implements AnalyticsQueryService {

    /** The model looks at the last 100 readings of the last 30 days. */
    private static final Duration HISTORY_WINDOW = Duration.ofDays(30);
    private static final int HISTORY_POINTS = 100;
    private static final Duration DEFAULT_RANGE = Duration.ofDays(7);
    private static final Duration MAX_RANGE = Duration.ofDays(31);

    private final SensorRepository sensorRepository;
    private final SensorReadingRepository readingRepository;
    private final ThresholdRuleRepository thresholdRuleRepository;
    private final TelemetryAnalyticsRepository analyticsRepository;
    private final PredictiveModel predictiveModel;
    private final MaintenanceContextFacade maintenanceContextFacade;
    private final Clock clock;
    private final double criticalHours;

    public AnalyticsQueryServiceImpl(SensorRepository sensorRepository,
                                     SensorReadingRepository readingRepository,
                                     ThresholdRuleRepository thresholdRuleRepository,
                                     TelemetryAnalyticsRepository analyticsRepository,
                                     PredictiveModel predictiveModel,
                                     MaintenanceContextFacade maintenanceContextFacade,
                                     Clock clock,
                                     @Value("${app.telemetry.rul-critical-hours:168}") double criticalHours) {
        this.sensorRepository = sensorRepository;
        this.readingRepository = readingRepository;
        this.thresholdRuleRepository = thresholdRuleRepository;
        this.analyticsRepository = analyticsRepository;
        this.predictiveModel = predictiveModel;
        this.maintenanceContextFacade = maintenanceContextFacade;
        this.clock = clock;
        this.criticalHours = criticalHours;
    }

    @Override
    @Transactional(readOnly = true)
    public RulResult handle(GetAssetRulQuery query) {
        requireAssetExists(query.tenantId(), query.assetId());
        return estimateRul(query.tenantId(), query.assetId(), clock.instant());
    }

    @Override
    @Transactional(readOnly = true)
    public AssetAnalytics handle(GetAssetAnalyticsQuery query) {
        requireAssetExists(query.tenantId(), query.assetId());

        Instant now = clock.instant();
        Instant to = query.to() != null ? query.to() : now;
        Instant from = query.from() != null ? query.from() : to.minus(DEFAULT_RANGE);
        if (from.isAfter(to)) {
            throw new DomainValidationException("validation.reading.range-invalid");
        }
        if (Duration.between(from, to).compareTo(MAX_RANGE) > 0) {
            throw new DomainValidationException("validation.reading.range-too-large");
        }

        Map<UUID, Sensor> sensors = sensorRepository.findByAssetId(query.tenantId(), query.assetId()).stream()
                .collect(Collectors.toMap(Sensor::getId, Function.identity()));
        PagedResult<DetectionView> detections =
                analyticsRepository.findDetections(query.tenantId(), sensors.keySet(), from, to, query.page());
        return new AssetAnalytics(detections, sensors, estimateRul(query.tenantId(), query.assetId(), now));
    }

    /**
     * The asset's RUL is the shortest one among its sensors: the variable that will reach its limit first.
     */
    private RulResult estimateRul(UUID tenantId, UUID assetId, Instant now) {
        List<Sensor> sensors = sensorRepository.findByAssetId(tenantId, assetId);
        Map<UUID, ThresholdRule> rules = thresholdRuleRepository
                .findBySensorIds(tenantId, sensors.stream().map(Sensor::getId).toList()).stream()
                .collect(Collectors.toMap(ThresholdRule::getSensorId, Function.identity()));

        RulResult best = null;
        double bestHours = Double.MAX_VALUE;
        for (Sensor sensor : sensors) {
            ThresholdRule rule = rules.get(sensor.getId());
            if (rule == null || !sensor.isActive()) {
                continue;
            }
            Optional<RulPrediction> prediction = predictiveModel.estimateRul(
                    recentHistory(tenantId, sensor.getId(), now), rule.getLowerBound(), rule.getUpperBound());
            if (prediction.isPresent() && prediction.get().hoursRemaining() < bestHours) {
                bestHours = prediction.get().hoursRemaining();
                best = RulResult.available(prediction.get(), predictiveModel.version(), now,
                        sensor.getMetric().name(), bestHours < criticalHours);
            }
        }
        return best != null ? best : RulResult.unavailable(predictiveModel.version(), now);
    }

    /** The newest 100 readings of the last 30 days, put in chronological order for the model. */
    private List<ReadingPoint> recentHistory(UUID tenantId, UUID sensorId, Instant now) {
        PagedResult<SensorReading> page = readingRepository.search(tenantId, List.of(sensorId),
                now.minus(HISTORY_WINDOW), now, new PageQuery(0, HISTORY_POINTS));
        List<ReadingPoint> points = new ArrayList<>();
        for (SensorReading reading : page.items()) {
            points.add(new ReadingPoint(reading.getMeasuredAt(), reading.getValue().doubleValue()));
        }
        Collections.reverse(points);
        return points;
    }

    private void requireAssetExists(UUID tenantId, UUID assetId) {
        if (maintenanceContextFacade.assetState(tenantId, assetId) == MaintenanceContextFacade.AssetState.NOT_FOUND) {
            throw new ApplicationException(TelemetryError.ASSET_NOT_FOUND);
        }
    }
}