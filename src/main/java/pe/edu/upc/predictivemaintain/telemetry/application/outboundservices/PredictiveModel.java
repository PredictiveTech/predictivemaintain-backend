package pe.edu.upc.predictivemaintain.telemetry.application.outboundservices;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Port to the model that estimates the remaining useful life. Any implementation can be plugged in
 * (a statistical model, a library, a remote service) without changing the use cases.
 */
public interface PredictiveModel {

    /** Identifier stored with every estimate so it can be traced to the model that produced it. */
    String version();

    /**
     * Estimates the hours left before the monitored variable reaches the limit it is drifting towards.
     * It returns empty when the history is not enough or shows no clear trend: never a made-up number.
     *
     * @param history oldest first
     */
    Optional<RulPrediction> estimateRul(List<ReadingPoint> history, BigDecimal lowerBound, BigDecimal upperBound);
}