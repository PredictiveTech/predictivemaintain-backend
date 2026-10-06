package pe.edu.upc.predictivemaintain.maintenance.interfaces.rest.transform;

import pe.edu.upc.predictivemaintain.maintenance.application.queryservices.AssetLabel;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.aggregates.Alert;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.valueobjects.AlertDiagnostic;
import pe.edu.upc.predictivemaintain.maintenance.interfaces.rest.resources.AlertDiagnosticResource;
import pe.edu.upc.predictivemaintain.maintenance.interfaces.rest.resources.AlertResource;

public final class AlertResourceFromEntityAssembler {

    private AlertResourceFromEntityAssembler() {
    }

    /** @param asset how to call the alert's asset; null leaves the code and name empty */
    public static AlertResource toResource(Alert alert, AssetLabel asset) {
        AlertDiagnostic diagnostic = alert.getDiagnostic();
        AlertDiagnosticResource diagnosticResource = diagnostic == null
                ? null
                : new AlertDiagnosticResource(diagnostic.metric(), diagnostic.unit(), diagnostic.observedValue(),
                diagnostic.lowerBound(), diagnostic.upperBound(), diagnostic.measuredAt());
        return new AlertResource(alert.getId(), alert.getAssetId(),
                asset == null ? null : asset.code(), asset == null ? null : asset.name(),
                alert.getSeverity(), alert.getStatus(), alert.getRaisedAt(), alert.getDiscardReason(),
                alert.getVersion(), diagnosticResource);
    }
}