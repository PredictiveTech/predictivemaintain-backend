package pe.edu.upc.predictivemaintain.notifications.application.commandservices;

import pe.edu.upc.predictivemaintain.notifications.domain.model.aggregates.DeviceToken;
import pe.edu.upc.predictivemaintain.notifications.domain.model.commands.RegisterDeviceCommand;
import pe.edu.upc.predictivemaintain.notifications.domain.model.commands.UnregisterDeviceCommand;

public interface DeviceCommandService {

    /** Registers the installation, or moves it to the current user if it was registered before. Idempotent. */
    DeviceToken handle(RegisterDeviceCommand command);

    void handle(UnregisterDeviceCommand command);
}