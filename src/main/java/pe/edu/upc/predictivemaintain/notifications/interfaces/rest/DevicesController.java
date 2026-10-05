package pe.edu.upc.predictivemaintain.notifications.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upc.predictivemaintain.iam.interfaces.acl.AuthenticatedUser;
import pe.edu.upc.predictivemaintain.notifications.application.commandservices.DeviceCommandService;
import pe.edu.upc.predictivemaintain.notifications.domain.model.commands.RegisterDeviceCommand;
import pe.edu.upc.predictivemaintain.notifications.domain.model.commands.UnregisterDeviceCommand;
import pe.edu.upc.predictivemaintain.notifications.interfaces.rest.resources.DeviceResource;
import pe.edu.upc.predictivemaintain.notifications.interfaces.rest.resources.RegisterDeviceResource;
import pe.edu.upc.predictivemaintain.notifications.interfaces.rest.transform.NotificationResourceAssembler;

import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/devices", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Devices", description = "Registers the phone to receive push notifications")
@SecurityRequirement(name = "bearerAuth")
public class DevicesController {

    private final DeviceCommandService deviceCommandService;

    public DevicesController(DeviceCommandService deviceCommandService) {
        this.deviceCommandService = deviceCommandService;
    }

    @PostMapping
    @Operation(summary = "Register this device for push notifications",
            description = "Open to every role. Call it after each login and whenever Firebase gives the app a new "
                    + "token. Registering a token that already exists moves it to the current user, so it is "
                    + "safe to repeat. Keep the returned id for the logout.")
    public ResponseEntity<DeviceResource> register(@AuthenticationPrincipal AuthenticatedUser principal,
                                                   @Valid @RequestBody RegisterDeviceResource resource) {
        var deviceToken = deviceCommandService.handle(new RegisterDeviceCommand(
                principal.tenantId(), principal.userId(), resource.token(), resource.platform()));
        return ResponseEntity.status(HttpStatus.CREATED).body(NotificationResourceAssembler.toResource(deviceToken));
    }

    @DeleteMapping("/{deviceId}")
    @Operation(summary = "Stop receiving push notifications on this device",
            description = "Call it on logout, so the next person who uses the phone does not get the previous "
                    + "user's alerts. 404 if the device is not yours.")
    public ResponseEntity<Void> unregister(@AuthenticationPrincipal AuthenticatedUser principal,
                                           @PathVariable UUID deviceId) {
        deviceCommandService.handle(new UnregisterDeviceCommand(principal.tenantId(), principal.userId(), deviceId));
        return ResponseEntity.noContent().build();
    }
}