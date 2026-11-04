package pe.edu.upc.predictivemaintain.telemetry.domain.services;

/**
 * @param anomalous         true when |z| is above the threshold
 * @param zScore            how many standard deviations the value is from the recent mean (0 if not evaluated)
 * @param samples           how many past readings were used; below the minimum nothing is evaluated
 */
public record StatisticalVerdict(boolean anomalous, double zScore, double mean, double standardDeviation, int samples) {

    public static StatisticalVerdict notEvaluated(int samples) {
        return new StatisticalVerdict(false, 0, 0, 0, samples);
    }
}
