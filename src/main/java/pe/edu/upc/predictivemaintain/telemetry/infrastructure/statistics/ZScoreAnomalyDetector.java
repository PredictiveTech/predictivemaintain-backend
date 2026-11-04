package pe.edu.upc.predictivemaintain.telemetry.infrastructure.statistics;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pe.edu.upc.predictivemaintain.telemetry.domain.services.StatisticalAnomalyDetector;
import pe.edu.upc.predictivemaintain.telemetry.domain.services.StatisticalVerdict;

/** SP-02. z = (x - mean) / stdDev over the last N readings, using Apache Commons Math. */
@Component
public class ZScoreAnomalyDetector implements StatisticalAnomalyDetector {

    private final boolean enabled;
    private final int windowSize;
    private final int minSamples;
    private final double zThreshold;

    public ZScoreAnomalyDetector(
            @Value("${app.telemetry.statistical.enabled:false}") boolean enabled,
            @Value("${app.telemetry.statistical.window-size:50}") int windowSize,
            @Value("${app.telemetry.statistical.min-samples:10}") int minSamples,
            @Value("${app.telemetry.statistical.z-threshold:3.0}") double zThreshold) {
        this.enabled = enabled;
        this.windowSize = windowSize;
        this.minSamples = minSamples;
        this.zThreshold = zThreshold;
    }

    @Override
    public boolean enabled() {
        return enabled;
    }

    @Override
    public int windowSize() {
        return windowSize;
    }

    @Override
    public StatisticalVerdict analyze(double[] history, double current) {
        // Too little history gives a meaningless deviation: better to say nothing than to raise false alarms
        if (history.length < minSamples) {
            return StatisticalVerdict.notEvaluated(history.length);
        }
        DescriptiveStatistics stats = new DescriptiveStatistics(history);
        double mean = stats.getMean();
        double std = stats.getStandardDeviation();
        if (std == 0) {
            // Perfectly constant signal: any change is infinitely far in z terms, so report it explicitly
            boolean changed = current != mean;
            return new StatisticalVerdict(changed, changed ? Double.POSITIVE_INFINITY : 0, mean, 0, history.length);
        }
        double z = (current - mean) / std;
        return new StatisticalVerdict(Math.abs(z) > zThreshold, z, mean, std, history.length);
    }
}
