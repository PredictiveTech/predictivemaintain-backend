package pe.edu.upc.predictivemaintain.maintenance.domain.services;

import java.time.Duration;
import java.time.Instant;

/**
 * Rules to decide what to do when an offline action reaches a server whose data may have moved on (US-28, scenario 3).
 */
public final class SyncConflictPolicy {

    /** Offline actions older than this are not accepted: after two weeks the field situation is surely different. */
    public static final Duration MAX_AGE = Duration.ofDays(14);

    private SyncConflictPolicy() {
    }

    /**
     * The most recent record wins: if the order changed on the server after the technician acted, the
     * server's version is kept and the technician is told.
     */
    public static boolean remoteIsMoreRecent(Instant lastRemoteChange, Instant actionTime) {
        return lastRemoteChange != null && lastRemoteChange.isAfter(actionTime);
    }

    public static boolean isTooOld(Instant actionTime, Instant now) {
        return actionTime.isBefore(now.minus(MAX_AGE));
    }
}