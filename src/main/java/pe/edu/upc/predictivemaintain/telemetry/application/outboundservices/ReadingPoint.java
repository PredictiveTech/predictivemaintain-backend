package pe.edu.upc.predictivemaintain.telemetry.application.outboundservices;

import java.time.Instant;

/**
 * One point of a sensor's history: when it was measured and its value.
 */
public record ReadingPoint(Instant at, double value) {
}