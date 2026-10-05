package pe.edu.upc.predictivemaintain.telemetry.application.queryservices;

import pe.edu.upc.predictivemaintain.telemetry.application.outboundservices.RulPrediction;

import java.time.Instant;

/**
 * Remaining useful life of an asset. When it is UNAVAILABLE every other value is null: the system says it
 * does not know instead of inventing a number.
 *
 * @param drivingMetric the variable that will reach its limit first
 */
public record RulResult(Availability availability, Double hoursRemaining, Double confidence, String modelVersion,
                        Instant estimatedAt, String drivingMetric, boolean interventionRecommended) {

    public enum Availability {
        AVAILABLE,
        UNAVAILABLE
    }

    public static RulResult available(RulPrediction prediction, String modelVersion, Instant estimatedAt,
                                      String drivingMetric, boolean interventionRecommended) {
        return new RulResult(Availability.AVAILABLE,
                Math.round(prediction.hoursRemaining() * 10.0) / 10.0,
                Math.round(prediction.confidence() * 1000.0) / 1000.0,
                modelVersion, estimatedAt, drivingMetric, interventionRecommended);
    }

    public static RulResult unavailable(String modelVersion, Instant estimatedAt) {
        return new RulResult(Availability.UNAVAILABLE, null, null, modelVersion, estimatedAt, null, false);
    }
}