package pe.edu.upc.predictivemaintain.maintenance.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.valueobjects.ClockOffset;
import pe.edu.upc.predictivemaintain.maintenance.domain.services.SyncConflictPolicy;
import pe.edu.upc.predictivemaintain.shared.domain.exceptions.DomainValidationException;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OfflineSyncRulesTest {

    private static final Instant SERVER_NOW = Instant.parse("2026-10-05T15:00:00Z");

    // ---------------------------------------------------------------- reloj del dispositivo

    @Test
    @DisplayName("US-28 - Un teléfono atrasado 10 minutos: sus horas se corrigen sumando 10 minutos")
    void behindDeviceIsShiftedForward() {
        ClockOffset offset = ClockOffset.between(Instant.parse("2026-10-05T14:50:00Z"), SERVER_NOW);

        assertThat(offset.seconds()).isEqualTo(600);
        assertThat(offset.toServerTime(Instant.parse("2026-10-05T13:50:00Z"), SERVER_NOW))
                .isEqualTo(Instant.parse("2026-10-05T14:00:00Z"));
    }

    @Test
    @DisplayName("US-28 - Un teléfono adelantado tiene un desfase negativo y sus horas se corrigen hacia atrás")
    void aheadDeviceIsShiftedBack() {
        ClockOffset offset = ClockOffset.between(Instant.parse("2026-10-05T15:05:00Z"), SERVER_NOW);

        assertThat(offset.seconds()).isEqualTo(-300);
        assertThat(offset.toServerTime(Instant.parse("2026-10-05T15:04:00Z"), SERVER_NOW))
                .isEqualTo(Instant.parse("2026-10-05T14:59:00Z"));
    }

    @Test
    @DisplayName("Una acción nunca queda en el futuro del servidor")
    void actionTimeIsNeverInTheFuture() {
        ClockOffset offset = ClockOffset.between(SERVER_NOW, SERVER_NOW);

        assertThat(offset.toServerTime(SERVER_NOW.plusSeconds(30), SERVER_NOW)).isEqualTo(SERVER_NOW);
    }

    @Test
    @DisplayName("Un reloj que difiere más de 24 horas no es confiable y se rechaza el lote")
    void aClockMoreThanADayOffIsRefused() {
        assertThat(ClockOffset.between(SERVER_NOW.minus(Duration.ofHours(24)), SERVER_NOW).seconds()).isEqualTo(86400);
        assertThatThrownBy(() -> ClockOffset.between(SERVER_NOW.minus(Duration.ofHours(25)), SERVER_NOW))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("validation.sync.clock-skew-too-large");
        assertThatThrownBy(() -> ClockOffset.between(SERVER_NOW.plus(Duration.ofHours(25)), SERVER_NOW))
                .isInstanceOf(DomainValidationException.class);
    }

    // ---------------------------------------------------------------- conflictos

    @Test
    @DisplayName("US-28 esc. 3 - Gana el registro más reciente: si el servidor cambió después de la acción, se conserva el del servidor")
    void theMoreRecentRecordWins() {
        Instant technicianActed = Instant.parse("2026-10-05T14:00:00Z");

        assertThat(SyncConflictPolicy.remoteIsMoreRecent(Instant.parse("2026-10-05T14:30:00Z"), technicianActed)).isTrue();
        assertThat(SyncConflictPolicy.remoteIsMoreRecent(Instant.parse("2026-10-05T13:30:00Z"), technicianActed)).isFalse();
    }

    @Test
    @DisplayName("Si el servidor cambió exactamente en el mismo instante, no hay conflicto: la acción se aplica")
    void aTieIsNotAConflict() {
        Instant sameInstant = Instant.parse("2026-10-05T14:00:00Z");

        assertThat(SyncConflictPolicy.remoteIsMoreRecent(sameInstant, sameInstant)).isFalse();
    }

    @Test
    @DisplayName("Una orden sin cambios previos no tiene conflicto")
    void noPreviousChangeMeansNoConflict() {
        assertThat(SyncConflictPolicy.remoteIsMoreRecent(null, SERVER_NOW)).isFalse();
    }

    @Test
    @DisplayName("Una acción de hace más de 14 días no se acepta; la de exactamente 14 días sí")
    void tooOldActionsAreRefused() {
        assertThat(SyncConflictPolicy.isTooOld(SERVER_NOW.minus(Duration.ofDays(14)), SERVER_NOW)).isFalse();
        assertThat(SyncConflictPolicy.isTooOld(SERVER_NOW.minus(Duration.ofDays(14)).minusSeconds(1), SERVER_NOW)).isTrue();
        assertThat(SyncConflictPolicy.isTooOld(SERVER_NOW.minusSeconds(60), SERVER_NOW)).isFalse();
    }
}