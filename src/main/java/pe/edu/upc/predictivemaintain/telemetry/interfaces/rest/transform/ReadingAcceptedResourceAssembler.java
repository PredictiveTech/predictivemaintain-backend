package pe.edu.upc.predictivemaintain.telemetry.interfaces.rest.transform;

import pe.edu.upc.predictivemaintain.telemetry.application.commandservices.IngestReadingResult;
import pe.edu.upc.predictivemaintain.telemetry.domain.model.aggregates.AnomalyDetection;
import pe.edu.upc.predictivemaintain.telemetry.interfaces.rest.resources.ReadingAcceptedResource;

public final class ReadingAcceptedResourceAssembler {

    private ReadingAcceptedResourceAssembler() {
    }

    public static ReadingAcceptedResource toResource(IngestReadingResult result) {
        AnomalyDetection detection = result.detection();
        return new ReadingAcceptedResource(
                result.reading().getId(),
                result.reading().getSensorId(),
                !result.created(),
                detection != null,
                detection != null && detection.isAlertRaised(),
                detection == null ? null : detection.getSeverity());
    }
}