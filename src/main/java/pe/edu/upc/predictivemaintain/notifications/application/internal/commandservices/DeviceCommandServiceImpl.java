package pe.edu.upc.predictivemaintain.notifications.application.internal.commandservices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.predictivemaintain.notifications.application.commandservices.DeviceCommandService;
import pe.edu.upc.predictivemaintain.notifications.application.errors.NotificationError;
import pe.edu.upc.predictivemaintain.notifications.domain.model.aggregates.DeviceToken;
import pe.edu.upc.predictivemaintain.notifications.domain.model.commands.RegisterDeviceCommand;
import pe.edu.upc.predictivemaintain.notifications.domain.model.commands.UnregisterDeviceCommand;
import pe.edu.upc.predictivemaintain.notifications.domain.repositories.DeviceTokenRepository;
import pe.edu.upc.predictivemaintain.shared.application.errors.ApplicationException;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

@Service
public class DeviceCommandServiceImpl implements DeviceCommandService {

    private final DeviceTokenRepository deviceTokenRepository;
    private final Clock clock;

    public DeviceCommandServiceImpl(DeviceTokenRepository deviceTokenRepository, Clock clock) {
        this.deviceTokenRepository = deviceTokenRepository;
        this.clock = clock;
    }

    @Override
    @Transactional
    public DeviceToken handle(RegisterDeviceCommand command) {
        Instant now = clock.instant();
        Optional<DeviceToken> existing = deviceTokenRepository.findByToken(command.token() == null ? "" : command.token().trim());
        if (existing.isPresent()) {
            DeviceToken deviceToken = existing.get();
            deviceToken.rebind(command.tenantId(), command.userId(), command.platform(), now);
            return deviceTokenRepository.save(deviceToken);
        }
        return deviceTokenRepository.save(
                DeviceToken.register(command.tenantId(), command.userId(), command.token(), command.platform(), now));
    }

    @Override
    @Transactional
    public void handle(UnregisterDeviceCommand command) {
        // Only the owner can remove a device: someone else's id behaves as if it did not exist.
        DeviceToken deviceToken = deviceTokenRepository.findByIdAndUserId(command.deviceId(), command.userId())
                .filter(candidate -> candidate.getTenantId().equals(command.tenantId()))
                .orElseThrow(() -> new ApplicationException(NotificationError.DEVICE_NOT_FOUND));
        deviceTokenRepository.delete(deviceToken.getId());
    }
}