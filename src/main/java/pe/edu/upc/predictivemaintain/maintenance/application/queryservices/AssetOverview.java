package pe.edu.upc.predictivemaintain.maintenance.application.queryservices;

import pe.edu.upc.predictivemaintain.maintenance.domain.model.aggregates.Asset;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.valueobjects.AssetStatus;

import java.util.List;

public record AssetOverview(Asset asset, AssetStatus status, List<String> sensorTypes) {
}