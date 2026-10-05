package pe.edu.upc.predictivemaintain.maintenance.application.internal.commandservices;

import org.springframework.stereotype.Service;
import pe.edu.upc.predictivemaintain.maintenance.application.commandservices.SyncCommandService;
import pe.edu.upc.predictivemaintain.maintenance.application.commandservices.SyncResult;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.commands.SyncOperation;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.commands.SyncWorkOrdersCommand;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.valueobjects.ClockOffset;

import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@Service
public class SyncCommandServiceImpl implements SyncCommandService {

    private final SyncOperationProcessor processor;
    private final Clock clock;

    public SyncCommandServiceImpl(SyncOperationProcessor processor, Clock clock) {
        this.processor = processor;
        this.clock = clock;
    }

    @Override
    public SyncResult handle(SyncWorkOrdersCommand command) {
        Instant serverNow = clock.instant();
        // Translates every timestamp of the batch from the device's clock to the server's.
        ClockOffset offset = ClockOffset.between(command.deviceSentAt(), serverNow);

        // Oldest action first, so a START is applied before the COMPLETE that follows it.
        // The sort is stable: actions with the same time keep the order the app sent them in.
        List<Timed> ordered = command.operations().stream()
                .map(operation -> new Timed(operation, offset.toServerTime(operation.performedAt(), serverNow)))
                .sorted(Comparator.comparing(Timed::actionTime))
                .toList();

        List<SyncResult.Item> items = ordered.stream()
                .map(timed -> processor.process(command.tenantId(), command.actorId(), timed.operation(),
                        timed.actionTime(), serverNow))
                .toList();
        return new SyncResult(serverNow, offset.seconds(), items);
    }

    private record Timed(SyncOperation operation, Instant actionTime) {
    }
}