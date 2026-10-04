package pe.edu.upc.predictivemaintain.maintenance.interfaces.rest.transform;

import pe.edu.upc.predictivemaintain.maintenance.domain.model.aggregates.Alert;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.valueobjects.AlertDiagnostic;
import pe.edu.upc.predictivemaintain.maintenance.interfaces.rest.resources.AlertDiagnosticResource;
import pe.edu.upc.predictivemaintain.maintenance.interfaces.rest.resources.AlertResource;

public final class AlertResourceFromEntityAssembler {

    private AlertResourceFromEntityAssembler() {
    }

    public static AlertResource toResource(Alert alert) {
        AlertDiagnostic diagnostic = alert.getDiagnostic();
        AlertDiagnosticResource diagnosticResource = diagnostic == null
                ? null
                : new AlertDiagnosticResource(diagnostic.metric(), diagnostic.unit(), diagnostic.observedValue(),
                diagnostic.lowerBound(), diagnostic.upperBound(), diagnostic.measuredAt());
        return new AlertResource(alert.getId(), alert.getAssetId(), alert.getSeverity(), alert.getStatus(),
                alert.getRaisedAt(), alert.getDiscardReason(), alert.getVersion(), diagnosticResource);
    }
}