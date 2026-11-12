package pe.edu.upc.predictivemaintain.notifications.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.edu.upc.predictivemaintain.notifications.application.internal.commandservices.DeviceCommandServiceImpl;
import pe.edu.upc.predictivemaintain.notifications.domain.model.aggregates.DeviceToken;
import pe.edu.upc.predictivemaintain.notifications.domain.model.commands.RegisterDeviceCommand;
import pe.edu.upc.predictivemaintain.notifications.domain.model.valueobjects.DevicePlatform;
import pe.edu.upc.predictivemaintain.notifications.domain.repositories.DeviceTokenRepository;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DeviceCommandServiceTest {

    private static final UUID TENANT = UUID.randomUUID();
    private static final UUID USER_A = UUID.randomUUID();
    private static final UUID USER_B = UUID.randomUUID();
    private static final String FCM_TOKEN = "fcm-token-123";

    private InMemoryDeviceTokenRepository repository;
    private MutableClock clock;
    private DeviceCommandServiceImpl service;

    @BeforeEach
    void setUp() {
        repository = new InMemoryDeviceTokenRepository();
        clock = new MutableClock(Instant.parse("2026-11-12T10:00:00Z"));
        service = new DeviceCommandServiceImpl(repository, clock);
    }

    @Test
    @DisplayName("Un token nuevo se crea con registeredAt y lastSeenAt iguales a la hora actual")
    void newTokenIsCreated() {
        DeviceToken saved = service.handle(new RegisterDeviceCommand(TENANT, USER_A, FCM_TOKEN, DevicePlatform.ANDROID));

        assertThat(repository.tokens).hasSize(1);
        assertThat(saved.getToken()).isEqualTo(FCM_TOKEN);
        assertThat(saved.getUserId()).isEqualTo(USER_A);
        assertThat(saved.getTenantId()).isEqualTo(TENANT);
        assertThat(saved.getPlatform()).isEqualTo(DevicePlatform.ANDROID);
        assertThat(saved.getRegisteredAt()).isEqualTo(clock.instant());
        assertThat(saved.getLastSeenAt()).isEqualTo(clock.instant());
    }

    @Test
    @DisplayName("Registrar el mismo token otra vez no duplica: actualiza lastSeenAt y conserva id y registeredAt")
    void sameTokenIsUpserted() {
        DeviceToken first = service.handle(new RegisterDeviceCommand(TENANT, USER_A, FCM_TOKEN, DevicePlatform.ANDROID));
        Instant firstSeen = clock.instant();
        clock.advanceMinutes(30);

        DeviceToken second = service.handle(new RegisterDeviceCommand(TENANT, USER_A, FCM_TOKEN, DevicePlatform.ANDROID));

        assertThat(repository.tokens).hasSize(1);
        assertThat(second.getId()).isEqualTo(first.getId());
        assertThat(second.getRegisteredAt()).isEqualTo(firstSeen);
        assertThat(second.getLastSeenAt()).isEqualTo(clock.instant()).isAfter(firstSeen);
    }

    @Test
    @DisplayName("Si otro usuario inicia sesión en el mismo teléfono, el token pasa a ese usuario")
    void tokenMovesToTheNewUser() {
        service.handle(new RegisterDeviceCommand(TENANT, USER_A, FCM_TOKEN, DevicePlatform.ANDROID));
        clock.advanceMinutes(5);

        DeviceToken moved = service.handle(new RegisterDeviceCommand(TENANT, USER_B, FCM_TOKEN, DevicePlatform.ANDROID));

        assertThat(repository.tokens).hasSize(1);
        assertThat(moved.getUserId()).isEqualTo(USER_B);
        assertThat(repository.findByTenantIdAndUserId(TENANT, USER_A)).isEmpty();
        assertThat(repository.findByTenantIdAndUserId(TENANT, USER_B)).hasSize(1);
    }

    @Test
    @DisplayName("El token se guarda sin espacios sobrantes y se reconoce al repetirlo con espacios")
    void tokenIsTrimmed() {
        service.handle(new RegisterDeviceCommand(TENANT, USER_A, "  " + FCM_TOKEN + "  ", DevicePlatform.ANDROID));
        service.handle(new RegisterDeviceCommand(TENANT, USER_A, FCM_TOKEN, DevicePlatform.ANDROID));

        assertThat(repository.tokens).hasSize(1);
        assertThat(repository.tokens.get(FCM_TOKEN).getToken()).isEqualTo(FCM_TOKEN);
    }

    // ---------------------------------------------------------------- dobles de prueba

    private static class InMemoryDeviceTokenRepository implements DeviceTokenRepository {
        final Map<String, DeviceToken> tokens = new HashMap<>();

        @Override
        public DeviceToken save(DeviceToken deviceToken) {
            tokens.put(deviceToken.getToken(), deviceToken);
            return deviceToken;
        }

        @Override
        public Optional<DeviceToken> findByToken(String token) {
            return Optional.ofNullable(tokens.get(token));
        }

        @Override
        public Optional<DeviceToken> findByIdAndUserId(UUID id, UUID userId) {
            return tokens.values().stream()
                    .filter(t -> t.getId().equals(id) && t.getUserId().equals(userId)).findFirst();
        }

        @Override
        public List<DeviceToken> findByTenantIdAndUserId(UUID tenantId, UUID userId) {
            return tokens.values().stream()
                    .filter(t -> t.getTenantId().equals(tenantId) && t.getUserId().equals(userId)).toList();
        }

        @Override
        public void delete(UUID id) {
            tokens.values().removeIf(t -> t.getId().equals(id));
        }
    }

    private static class MutableClock extends Clock {
        private Instant now;

        MutableClock(Instant now) {
            this.now = now;
        }

        void advanceMinutes(long minutes) {
            now = now.plusSeconds(minutes * 60);
        }

        @Override
        public java.time.ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }
}
