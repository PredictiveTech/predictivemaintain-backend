package pe.edu.upc.predictivemaintain.maintenance.application.internal.queryservices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.predictivemaintain.maintenance.application.queryservices.AlertLabel;
import pe.edu.upc.predictivemaintain.maintenance.application.queryservices.AssetLabel;
import pe.edu.upc.predictivemaintain.maintenance.application.queryservices.LabelQueryService;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.aggregates.Alert;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.aggregates.Asset;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.queries.GetAlertLabelsQuery;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.queries.GetAssetLabelsQuery;
import pe.edu.upc.predictivemaintain.maintenance.domain.repositories.AlertRepository;
import pe.edu.upc.predictivemaintain.maintenance.domain.repositories.AssetRepository;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class LabelQueryServiceImpl implements LabelQueryService {

    private final AssetRepository assetRepository;
    private final AlertRepository alertRepository;

    public LabelQueryServiceImpl(AssetRepository assetRepository, AlertRepository alertRepository) {
        this.assetRepository = assetRepository;
        this.alertRepository = alertRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<UUID, AssetLabel> handle(GetAssetLabelsQuery query) {
        Set<UUID> ids = new HashSet<>(query.assetIds());
        if (ids.isEmpty()) {
            return Map.of(); // an empty IN (...) is not valid SQL on every database
        }
        Map<UUID, AssetLabel> labels = new HashMap<>();
        for (Asset asset : assetRepository.findAllByIdsAndTenantId(ids, query.tenantId())) {
            labels.put(asset.getId(), new AssetLabel(asset.getCode(), asset.getName()));
        }
        return labels;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<UUID, AlertLabel> handle(GetAlertLabelsQuery query) {
        Set<UUID> ids = new HashSet<>(query.alertIds());
        if (ids.isEmpty()) {
            return Map.of();
        }
        List<Alert> alerts = alertRepository.findAllByIdsAndTenantId(ids, query.tenantId());
        Map<UUID, AssetLabel> assets = handle(new GetAssetLabelsQuery(query.tenantId(),
                alerts.stream().map(Alert::getAssetId).toList()));

        Map<UUID, AlertLabel> labels = new HashMap<>();
        for (Alert alert : alerts) {
            AssetLabel asset = assets.get(alert.getAssetId());
            labels.put(alert.getId(), new AlertLabel(alert.getAssetId(),
                    asset == null ? null : asset.code(), asset == null ? null : asset.name(), alert.getSeverity()));
        }
        return labels;
    }
}