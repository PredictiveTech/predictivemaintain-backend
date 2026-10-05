package pe.edu.upc.predictivemaintain.telemetry.application.outboundservices;

/**
 * @param hoursRemaining hours until the variable reaches the limit it drifts towards (0 or more)
 * @param confidence     between 0 and 1: how well the model explains the history
 */
public record RulPrediction(double hoursRemaining, double confidence) {
}