package pe.edu.upc.predictivemaintain.maintenance.interfaces.rest.transform;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import pe.edu.upc.predictivemaintain.maintenance.application.commandservices.SyncResult;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.commands.SyncOperation;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.commands.SyncWorkOrdersCommand;
import pe.edu.upc.predictivemaintain.maintenance.interfaces.rest.resources.SyncRequestResource;
import pe.edu.upc.predictivemaintain.maintenance.interfaces.rest.resources.SyncResponseResource;

import java.util.UUID;

public final class SyncResourceAssembler {

    private SyncResourceAssembler() {
    }

    public static SyncWorkOrdersCommand toCommand(UUID tenantId, UUID actorId, SyncRequestResource resource) {
        return new SyncWorkOrdersCommand(tenantId, actorId, resource.deviceSentAt(),
                resource.operations().stream()
                        .map(operation -> new SyncOperation(operation.operationId(), operation.workOrderId(),
                                operation.type(), operation.performedAt(), operation.summary()))
                        .toList());
    }

    /** The explanations are translated here, with the language the app asked for in Accept-Language. */
    public static SyncResponseResource toResource(SyncResult result, MessageSource messages) {
        return new SyncResponseResource(result.serverTime(), result.clockOffsetSeconds(),
                result.items().stream()
                        .map(item -> new SyncResponseResource.Item(item.operationId(), item.outcome(),
                                messages.getMessage(item.messageKey(), item.messageArgs(), item.messageKey(),
                                        LocaleContextHolder.getLocale()),
                                item.workOrderStatus(), item.workOrderVersion()))
                        .toList());
    }
}