package pe.edu.upc.predictivemaintain.telemetry.application.queryservices;

import pe.edu.upc.predictivemaintain.shared.domain.model.valueobjects.PagedResult;
import pe.edu.upc.predictivemaintain.telemetry.domain.model.aggregates.Sensor;
import pe.edu.upc.predictivemaintain.telemetry.domain.model.valueobjects.DetectionView;

import java.util.Map;
import java.util.UUID;

public record AssetAnalytics(PagedResult<DetectionView> detections, Map<UUID, Sensor> sensors, RulResult rul) {
}