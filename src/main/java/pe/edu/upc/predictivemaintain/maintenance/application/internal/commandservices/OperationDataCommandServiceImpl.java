package pe.edu.upc.predictivemaintain.maintenance.application.internal.commandservices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.predictivemaintain.maintenance.application.commandservices.OperationDataCommandService;
import pe.edu.upc.predictivemaintain.maintenance.application.errors.MaintenanceError;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.aggregates.DowntimeInterval;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.aggregates.ProductionWindow;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.commands.RecordDowntimeCommand;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.commands.RecordProductionWindowCommand;
import pe.edu.upc.predictivemaintain.maintenance.domain.repositories.AssetRepository;
import pe.edu.upc.predictivemaintain.maintenance.domain.repositories.DowntimeIntervalRepository;
import pe.edu.upc.predictivemaintain.maintenance.domain.repositories.ProductionWindowRepository;
import pe.edu.upc.predictivemaintain.shared.application.errors.ApplicationException;

import java.time.Clock;
import java.util.UUID;

@Service
public class OperationDataCommandServiceImpl implements OperationDataCommandService {

    private final AssetRepository assetRepository;
    private final DowntimeIntervalRepository downtimeRepository;
    private final ProductionWindowRepository productionWindowRepository;
    private final Clock clock;

    public OperationDataCommandServiceImpl(AssetRepository assetRepository,
                                           DowntimeIntervalRepository downtimeRepository,
                                           ProductionWindowRepository productionWindowRepository,
                                           Clock clock) {
        this.assetRepository = assetRepository;
        this.downtimeRepository = downtimeRepository;
        this.productionWindowRepository = productionWindowRepository;
        this.clock = clock;
    }

    @Override
    @Transactional
    public DowntimeInterval handle(RecordDowntimeCommand command) {
        requireAsset(command.tenantId(), command.assetId());
        return downtimeRepository.save(DowntimeInterval.record(command.tenantId(), command.assetId(),
                command.startedAt(), command.endedAt(), clock.instant()));
    }

    @Override
    @Transactional
    public ProductionWindow handle(RecordProductionWindowCommand command) {
        requireAsset(command.tenantId(), command.assetId());
        return productionWindowRepository.save(ProductionWindow.record(command.tenantId(), command.assetId(),
                command.startsAt(), command.endsAt(), command.plannedSeconds(), command.operatingSeconds(),
                command.totalUnits(), command.goodUnits(), command.idealCycleSeconds(), clock.instant()));
    }

    /** An inactive asset still accepts late records of its past operation. */
    private void requireAsset(UUID tenantId, UUID assetId) {
        if (assetRepository.findByIdAndTenantId(assetId, tenantId).isEmpty()) {
            throw new ApplicationException(MaintenanceError.ASSET_NOT_FOUND);
        }
    }
}