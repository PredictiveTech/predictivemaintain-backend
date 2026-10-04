package pe.edu.upc.predictivemaintain.telemetry.application.queryservices;

import pe.edu.upc.predictivemaintain.telemetry.domain.model.queries.GetAssetReadingsQuery;
import pe.edu.upc.predictivemaintain.telemetry.domain.model.queries.GetAssetSensorsQuery;

import java.util.List;

public interface TelemetryQueryService {

    List<SensorPanelItem> handle(GetAssetSensorsQuery query);

    AssetReadings handle(GetAssetReadingsQuery query);
}