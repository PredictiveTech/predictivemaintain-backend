package pe.edu.upc.predictivemaintain.maintenance.interfaces.rest.transform;

import pe.edu.upc.predictivemaintain.maintenance.domain.model.aggregates.Alert;
import pe.edu.upc.predictivemaintain.maintenance.interfaces.rest.resources.AlertResource;

public final class AlertResourceFromEntityAssembler {

    private AlertResourceFromEntityAssembler() {
    }

    public static AlertResource toResource(Alert alert) {
        return new AlertResource(alert.getId(), alert.getAssetId(), alert.getSeverity(), alert.getStatus(),
                alert.getRaisedAt(), alert.getDiscardReason(), alert.getVersion());
    }
}