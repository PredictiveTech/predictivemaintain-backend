package pe.edu.upc.predictivemaintain.telemetry.interfaces.rest.resources;

import pe.edu.upc.predictivemaintain.shared.interfaces.rest.resources.PagedResource;

public record AnalyticsResource(PagedResource<DetectionResource> detections, RulResource rul) {
}