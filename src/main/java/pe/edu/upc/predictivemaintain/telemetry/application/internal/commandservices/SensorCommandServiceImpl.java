package pe.edu.upc.predictivemaintain.telemetry.application.internal.commandservices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.predictivemaintain.maintenance.interfaces.acl.MaintenanceContextFacade;
import pe.edu.upc.predictivemaintain.shared.application.errors.ApplicationException;
import pe.edu.upc.predictivemaintain.telemetry.application.commandservices.RegisteredSensor;
import pe.edu.upc.predictivemaintain.telemetry.application.commandservices.SensorCommandService;
import pe.edu.upc.predictivemaintain.telemetry.application.errors.TelemetryError;
import pe.edu.upc.predictivemaintain.telemetry.application.internal.support.DeviceKeySupport;
import pe.edu.upc.predictivemaintain.telemetry.domain.model.aggregates.Sensor;
import pe.edu.upc.predictivemaintain.telemetry.domain.model.aggregates.ThresholdRule;
import pe.edu.upc.predictivemaintain.telemetry.domain.model.commands.ConfigureThresholdCommand;
import pe.edu.upc.predictivemaintain.telemetry.domain.model.commands.RegisterSensorCommand;
import pe.edu.upc.predictivemaintain.telemetry.domain.repositories.SensorRepository;
import pe.edu.upc.predictivemaintain.telemetry.domain.repositories.ThresholdRuleRepository;

@Service
public class SensorCommandServiceImpl implements SensorCommandService {

    private final SensorRepository sensorRepository;
    private final ThresholdRuleRepository thresholdRuleRepository;
    private final MaintenanceContextFacade maintenanceContextFacade;

    public SensorCommandServiceImpl(SensorRepository sensorRepository,
                                    ThresholdRuleRepository thresholdRuleRepository,
                                    MaintenanceContextFacade maintenanceContextFacade) {
        this.sensorRepository = sensorRepository;
        this.thresholdRuleRepository = thresholdRuleRepository;
        this.maintenanceContextFacade = maintenanceContextFacade;
    }

    @Override
    @Transactional
    public RegisteredSensor handle(RegisterSensorCommand command) {
        MaintenanceContextFacade.AssetState state =
                maintenanceContextFacade.assetState(command.tenantId(), command.assetId());
        if (state == MaintenanceContextFacade.AssetState.NOT_FOUND) {
            throw new ApplicationException(TelemetryError.ASSET_NOT_FOUND);
        }
        if (state == MaintenanceContextFacade.AssetState.INACTIVE) {
            throw new ApplicationException(TelemetryError.ASSET_INACTIVE);
        }

        String deviceKey = DeviceKeySupport.generate();
        Sensor sensor = Sensor.register(command.tenantId(), command.assetId(), command.metric(),
                command.unit(), DeviceKeySupport.hash(deviceKey));
        return new RegisteredSensor(sensorRepository.save(sensor), deviceKey);
    }

    @Override
    @Transactional
    public ThresholdRule handle(ConfigureThresholdCommand command) {
        Sensor sensor = sensorRepository.findByIdAndTenantId(command.sensorId(), command.tenantId())
                .orElseThrow(() -> new ApplicationException(TelemetryError.SENSOR_NOT_FOUND));

        ThresholdRule rule = thresholdRuleRepository.findBySensorId(command.tenantId(), sensor.getId())
                .map(existing -> {
                    existing.change(command.lowerBound(), command.upperBound(), command.severity());
                    return existing;
                })
                .orElseGet(() -> ThresholdRule.create(command.tenantId(), sensor.getId(),
                        command.lowerBound(), command.upperBound(), command.severity()));
        return thresholdRuleRepository.save(rule);
    }
}