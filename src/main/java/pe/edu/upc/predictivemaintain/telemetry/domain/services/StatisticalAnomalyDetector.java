package pe.edu.upc.predictivemaintain.telemetry.domain.services;

/** SP-02. Decides if a value is a statistical outlier compared with the sensor's recent past. */
public interface StatisticalAnomalyDetector {

    boolean enabled();

    /** How many past readings the detector wants to look at. */
    int windowSize();

    /** @param history past readings, without the current one */
    StatisticalVerdict analyze(double[] history, double current);
}
