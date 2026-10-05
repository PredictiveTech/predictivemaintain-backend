package pe.edu.upc.predictivemaintain.telemetry.infrastructure.model;

import org.springframework.stereotype.Component;
import pe.edu.upc.predictivemaintain.telemetry.application.outboundservices.PredictiveModel;
import pe.edu.upc.predictivemaintain.telemetry.application.outboundservices.ReadingPoint;
import pe.edu.upc.predictivemaintain.telemetry.application.outboundservices.RulPrediction;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Baseline model: fits a straight line to the recent readings (ordinary least squares) and extrapolates
 * when it would reach the limit it is heading to. It is a statistical estimate, not a trained model.
 */
@Component
public class LinearTrendModel implements PredictiveModel {

    public static final String VERSION = "linear-trend-v1";

    private static final int MIN_POINTS = 10;
    private static final double MIN_CONFIDENCE = 0.5;
    private static final double MAX_HOURS = 24.0 * 365 * 10;
    private static final double MILLIS_PER_HOUR = 3_600_000.0;

    @Override
    public String version() {
        return VERSION;
    }

    @Override
    public Optional<RulPrediction> estimateRul(List<ReadingPoint> history, BigDecimal lowerBound,
                                               BigDecimal upperBound) {
        int n = history.size();
        if (n < MIN_POINTS) {
            return Optional.empty();
        }

        // x = hours since the first reading, y = measured value
        Instant origin = history.get(0).at();
        double[] hours = new double[n];
        double[] values = new double[n];
        double sumHours = 0;
        double sumValues = 0;
        for (int i = 0; i < n; i++) {
            hours[i] = Duration.between(origin, history.get(i).at()).toMillis() / MILLIS_PER_HOUR;
            values[i] = history.get(i).value();
            sumHours += hours[i];
            sumValues += values[i];
        }
        double meanHours = sumHours / n;
        double meanValues = sumValues / n;

        double sxx = 0;
        double sxy = 0;
        double syy = 0;
        for (int i = 0; i < n; i++) {
            double dx = hours[i] - meanHours;
            double dy = values[i] - meanValues;
            sxx += dx * dx;
            sxy += dx * dy;
            syy += dy * dy;
        }
        // All readings at the same instant, or perfectly flat: there is no trend to extrapolate.
        if (sxx == 0 || syy == 0) {
            return Optional.empty();
        }

        double slope = sxy / sxx;
        double intercept = meanValues - slope * meanHours;
        double rSquared = (sxy * sxy) / (sxx * syy);
        if (rSquared < MIN_CONFIDENCE) {
            return Optional.empty();
        }

        // Where the line says the variable is now, and which limit it is heading to.
        double latestFit = intercept + slope * hours[n - 1];
        double limit = slope > 0 ? upperBound.doubleValue() : lowerBound.doubleValue();
        // If the line is already beyond the limit the result would be negative: the time left is zero.
        double remaining = Math.max(0.0, (limit - latestFit) / slope);
        if (remaining > MAX_HOURS) {
            return Optional.empty();
        }
        return Optional.of(new RulPrediction(remaining, Math.min(1.0, rSquared)));
    }
}