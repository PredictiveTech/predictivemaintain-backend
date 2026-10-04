package pe.edu.upc.predictivemaintain.maintenance.application.internal.queryservices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.predictivemaintain.maintenance.application.errors.MaintenanceError;
import pe.edu.upc.predictivemaintain.maintenance.application.errors.ReportError;
import pe.edu.upc.predictivemaintain.maintenance.application.queryservices.AssetEvent;
import pe.edu.upc.predictivemaintain.maintenance.application.queryservices.AssetEvent.EventType;
import pe.edu.upc.predictivemaintain.maintenance.application.queryservices.AvailabilityReport;
import pe.edu.upc.predictivemaintain.maintenance.application.queryservices.ComparisonRow;
import pe.edu.upc.predictivemaintain.maintenance.application.queryservices.HistoryRow;
import pe.edu.upc.predictivemaintain.maintenance.application.queryservices.OeeReport;
import pe.edu.upc.predictivemaintain.maintenance.application.queryservices.ReportQueryService;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.aggregates.Alert;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.aggregates.Asset;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.aggregates.DowntimeInterval;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.aggregates.ProductionWindow;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.queries.GetAvailabilityReportQuery;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.queries.GetComparisonReportQuery;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.queries.GetHistoryExportQuery;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.queries.GetOeeReportQuery;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.valueobjects.AlertSeverity;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.valueobjects.ReportPeriod;
import pe.edu.upc.predictivemaintain.maintenance.domain.repositories.AssetActivityRepository;
import pe.edu.upc.predictivemaintain.maintenance.domain.repositories.AssetRepository;
import pe.edu.upc.predictivemaintain.maintenance.domain.repositories.DowntimeIntervalRepository;
import pe.edu.upc.predictivemaintain.maintenance.domain.repositories.ProductionWindowRepository;
import pe.edu.upc.predictivemaintain.maintenance.domain.services.AvailabilityCalculator;
import pe.edu.upc.predictivemaintain.maintenance.domain.services.OeeCalculator;
import pe.edu.upc.predictivemaintain.shared.application.errors.ApplicationException;
import pe.edu.upc.predictivemaintain.shared.domain.exceptions.DomainValidationException;

import java.time.Clock;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ReportQueryServiceImpl implements ReportQueryService {

    private static final int MAX_ASSETS = 20;

    private final AssetRepository assetRepository;
    private final AssetActivityRepository activityRepository;
    private final DowntimeIntervalRepository downtimeRepository;
    private final ProductionWindowRepository productionWindowRepository;
    private final Clock clock;

    public ReportQueryServiceImpl(AssetRepository assetRepository,
                                  AssetActivityRepository activityRepository,
                                  DowntimeIntervalRepository downtimeRepository,
                                  ProductionWindowRepository productionWindowRepository,
                                  Clock clock) {
        this.assetRepository = assetRepository;
        this.activityRepository = activityRepository;
        this.downtimeRepository = downtimeRepository;
        this.productionWindowRepository = productionWindowRepository;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public AvailabilityReport handle(GetAvailabilityReportQuery query) {
        ReportPeriod period = ReportPeriod.until(query.from(), query.to(), clock.instant());
        Asset asset = findAsset(query.tenantId(), query.assetId());
        Activity activity = loadActivity(query.tenantId(), List.of(asset.getId()), period);
        return buildAvailability(asset, period, activity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComparisonRow> handle(GetComparisonReportQuery query) {
        ReportPeriod period = ReportPeriod.until(query.from(), query.to(), clock.instant());
        List<Asset> assets = findAssets(query.tenantId(), query.assetIds());
        Activity activity = loadActivity(query.tenantId(), assets.stream().map(Asset::getId).toList(), period);

        List<ComparisonRow> rows = new ArrayList<>();
        for (Asset asset : assets) {
            List<Alert> alerts = activity.alerts().getOrDefault(asset.getId(), List.of());
            List<DowntimeInterval> stops = activity.stops().getOrDefault(asset.getId(), List.of());
            long downtime = AvailabilityCalculator.downtimeSeconds(period, stops);
            rows.add(new ComparisonRow(asset.getId(), asset.getCode(), asset.getName(), alerts.size(),
                    (int) alerts.stream().filter(alert -> alert.getSeverity() == AlertSeverity.CRITICAL).count(),
                    stops.size(), downtime,
                    AvailabilityCalculator.availabilityPercent(period.seconds(), downtime)));
        }
        return rows;
    }

    @Override
    @Transactional(readOnly = true)
    public List<HistoryRow> handle(GetHistoryExportQuery query) {
        ReportPeriod period = ReportPeriod.until(query.from(), query.to(), clock.instant());
        List<Asset> assets = findAssets(query.tenantId(), query.assetIds());
        Activity activity = loadActivity(query.tenantId(), assets.stream().map(Asset::getId).toList(), period);

        List<HistoryRow> rows = new ArrayList<>();
        for (Asset asset : assets) {
            for (AssetEvent event : eventsOf(asset, activity)) {
                rows.add(new HistoryRow(asset.getCode(), asset.getName(), event));
            }
        }
        if (rows.isEmpty()) {
            throw new ApplicationException(ReportError.REPORT_EMPTY);
        }
        rows.sort(Comparator.comparing((HistoryRow row) -> row.event().occurredAt()));
        return rows;
    }

    @Override
    @Transactional(readOnly = true)
    public OeeReport handle(GetOeeReportQuery query) {
        ReportPeriod period = ReportPeriod.until(query.from(), query.to(), clock.instant());
        Asset asset = findAsset(query.tenantId(), query.assetId());
        List<ProductionWindow> windows = productionWindowRepository.findStartingBetween(
                query.tenantId(), List.of(asset.getId()), period.from(), period.to());

        OeeCalculator.OeeFigures figures = OeeCalculator.compute(windows);
        List<String> unavailable = new ArrayList<>();
        if (figures.availability() == null) {
            unavailable.add("AVAILABILITY");
        }
        if (figures.performance() == null) {
            unavailable.add("PERFORMANCE");
        }
        if (figures.quality() == null) {
            unavailable.add("QUALITY");
        }
        return new OeeReport(asset.getId(), asset.getCode(), period, windows.size(), figures.availability(),
                figures.performance(), figures.quality(), figures.oee(), unavailable);
    }

    private AvailabilityReport buildAvailability(Asset asset, ReportPeriod period, Activity activity) {
        List<DowntimeInterval> stops = activity.stops().getOrDefault(asset.getId(), List.of());
        long downtime = AvailabilityCalculator.downtimeSeconds(period, stops);
        long periodSeconds = period.seconds();
        return new AvailabilityReport(asset.getId(), asset.getCode(), period,
                AvailabilityCalculator.availabilityPercent(periodSeconds, downtime), downtime, periodSeconds,
                eventsOf(asset, activity));
    }

    /** Alerts and stops of one asset, oldest first. */
    private List<AssetEvent> eventsOf(Asset asset, Activity activity) {
        Stream<AssetEvent> alerts = activity.alerts().getOrDefault(asset.getId(), List.of()).stream()
                .map(this::alertEvent);
        Stream<AssetEvent> stops = activity.stops().getOrDefault(asset.getId(), List.of()).stream()
                .map(this::downtimeEvent);
        return Stream.concat(alerts, stops)
                .sorted(Comparator.comparing(AssetEvent::occurredAt))
                .toList();
    }

    private AssetEvent alertEvent(Alert alert) {
        return new AssetEvent(EventType.ALERT, alert.getAssetId(), alert.getId(), alert.getRaisedAt(), null,
                alert.getSeverity().name(), alert.getStatus().name());
    }

    private AssetEvent downtimeEvent(DowntimeInterval stop) {
        return new AssetEvent(EventType.DOWNTIME, stop.getAssetId(), stop.getId(), stop.getStartedAt(),
                stop.getEndedAt(), null, null);
    }

    private Activity loadActivity(UUID tenantId, Collection<UUID> assetIds, ReportPeriod period) {
        Map<UUID, List<Alert>> alerts = activityRepository
                .findAlertsRaisedBetween(tenantId, assetIds, period.from(), period.to()).stream()
                .collect(Collectors.groupingBy(Alert::getAssetId));
        Map<UUID, List<DowntimeInterval>> stops = downtimeRepository
                .findOverlapping(tenantId, assetIds, period.from(), period.to()).stream()
                .collect(Collectors.groupingBy(DowntimeInterval::getAssetId));
        return new Activity(alerts, stops);
    }

    private Asset findAsset(UUID tenantId, UUID assetId) {
        return assetRepository.findByIdAndTenantId(assetId, tenantId)
                .orElseThrow(() -> new ApplicationException(MaintenanceError.ASSET_NOT_FOUND));
    }

    /** Between 1 and 20 distinct assets, all of them from the company; the order of the request is kept. */
    private List<Asset> findAssets(UUID tenantId, List<UUID> assetIds) {
        if (assetIds == null || assetIds.isEmpty()) {
            throw new DomainValidationException("validation.report.assets-invalid");
        }
        List<UUID> distinct = new ArrayList<>(new LinkedHashSet<>(assetIds));
        if (distinct.size() > MAX_ASSETS) {
            throw new DomainValidationException("validation.report.assets-invalid");
        }
        return distinct.stream().map(id -> findAsset(tenantId, id)).toList();
    }

    private record Activity(Map<UUID, List<Alert>> alerts, Map<UUID, List<DowntimeInterval>> stops) {
    }
}