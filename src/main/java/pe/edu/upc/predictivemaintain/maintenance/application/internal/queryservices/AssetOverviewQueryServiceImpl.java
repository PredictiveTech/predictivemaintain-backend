package pe.edu.upc.predictivemaintain.maintenance.application.internal.queryservices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.predictivemaintain.maintenance.application.queryservices.AssetOverview;
import pe.edu.upc.predictivemaintain.maintenance.application.queryservices.AssetOverviewQueryService;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.aggregates.Asset;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.queries.GetAssetOverviewsQuery;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.valueobjects.AssetStatus;
import pe.edu.upc.predictivemaintain.maintenance.domain.repositories.AssetActivityRepository;
import pe.edu.upc.predictivemaintain.maintenance.domain.repositories.AssetRepository;
import pe.edu.upc.predictivemaintain.shared.domain.model.valueobjects.PageQuery;
import pe.edu.upc.predictivemaintain.shared.domain.model.valueobjects.PagedResult;
import pe.edu.upc.predictivemaintain.telemetry.interfaces.acl.TelemetryContextFacade;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class AssetOverviewQueryServiceImpl implements AssetOverviewQueryService {

    private static final int CHUNK_SIZE = 100;

    private final AssetRepository assetRepository;
    private final AssetActivityRepository activityRepository;
    private final TelemetryContextFacade telemetryContextFacade;

    public AssetOverviewQueryServiceImpl(AssetRepository assetRepository,
                                         AssetActivityRepository activityRepository,
                                         TelemetryContextFacade telemetryContextFacade) {
        this.assetRepository = assetRepository;
        this.activityRepository = activityRepository;
        this.telemetryContextFacade = telemetryContextFacade;
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResult<AssetOverview> handle(GetAssetOverviewsQuery query) {
        String productionLine = blankToNull(query.productionLine());
        String assetType = blankToNull(query.assetType());
        String sensorType = blankToNull(query.sensorType());

        // Without status or sensor-type filters the database can paginate directly.
        if (query.status() == null && sensorType == null) {
            PagedResult<Asset> page = assetRepository.search(query.tenantId(), productionLine, assetType,
                    query.includeInactive(), query.page());
            return new PagedResult<>(describe(query.tenantId(), page.items()), page.totalElements(),
                    page.page(), page.size());
        }

        // The status and the sensor types are computed, not stored, so they cannot be filtered in the database:
        // describe every asset of the company (at most a few hundred, the plan limit), filter, then paginate.
        List<AssetOverview> matching = describe(query.tenantId(),
                loadAll(query.tenantId(), productionLine, assetType, query.includeInactive())).stream()
                .filter(overview -> query.status() == null || overview.status() == query.status())
                .filter(overview -> sensorType == null
                        || overview.sensorTypes().contains(sensorType.toUpperCase(Locale.ROOT)))
                .toList();

        int from = Math.min(query.page().page() * query.page().size(), matching.size());
        int to = Math.min(from + query.page().size(), matching.size());
        return new PagedResult<>(matching.subList(from, to), matching.size(),
                query.page().page(), query.page().size());
    }

    private List<Asset> loadAll(UUID tenantId, String productionLine, String assetType, boolean includeInactive) {
        List<Asset> all = new ArrayList<>();
        int page = 0;
        PagedResult<Asset> chunk;
        do {
            chunk = assetRepository.search(tenantId, productionLine, assetType, includeInactive,
                    new PageQuery(page++, CHUNK_SIZE));
            all.addAll(chunk.items());
        } while (!chunk.items().isEmpty() && all.size() < chunk.totalElements());
        return all;
    }

    /** Two queries for the whole list (alerts and sensors), not one per asset. */
    private List<AssetOverview> describe(UUID tenantId, List<Asset> assets) {
        if (assets.isEmpty()) {
            return List.of();
        }
        List<UUID> ids = assets.stream().map(Asset::getId).toList();
        Set<UUID> inAlert = activityRepository.findAssetIdsWithOpenAlerts(tenantId, ids);
        Map<UUID, TelemetryContextFacade.AssetTelemetry> telemetry = telemetryContextFacade.summarize(tenantId, ids);

        return assets.stream().map(asset -> {
            TelemetryContextFacade.AssetTelemetry summary = telemetry.get(asset.getId());
            boolean communicating = summary != null && summary.communicating();
            AssetStatus status;
            if (!asset.isActive()) {
                status = AssetStatus.INACTIVE;
            } else if (inAlert.contains(asset.getId())) {
                status = AssetStatus.IN_ALERT;
            } else {
                status = communicating ? AssetStatus.OPERATIONAL : AssetStatus.NO_COMMUNICATION;
            }
            List<String> sensorTypes = summary == null ? List.of() : List.copyOf(summary.metrics());
            return new AssetOverview(asset, status, sensorTypes);
        }).toList();
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}