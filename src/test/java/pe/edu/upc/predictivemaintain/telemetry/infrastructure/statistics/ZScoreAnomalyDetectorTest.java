package pe.edu.upc.predictivemaintain.telemetry.infrastructure.statistics;

import org.junit.jupiter.api.Test;
import pe.edu.upc.predictivemaintain.telemetry.domain.services.StatisticalVerdict;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ZScoreAnomalyDetectorTest {

    private final ZScoreAnomalyDetector detector = new ZScoreAnomalyDetector(true, 50, 10, 3.0);
    private final double[] normal = {70, 71, 69, 70.5, 69.5, 70, 71, 69, 70.2, 69.8, 70.4, 69.6};

    @Test
    void valueInsideTheUsualSpreadIsNotAnomalous() {
        assertFalse(detector.analyze(normal, 70.8).anomalous());
    }

    @Test
    void valueFarFromTheRecentMeanIsAnomalousEvenIfInsideTheThresholdRange() {
        // 78 could be inside a configured range of 0..100, but it is far from what this sensor usually reads
        StatisticalVerdict verdict = detector.analyze(normal, 78);
        assertTrue(verdict.anomalous());
        assertTrue(verdict.zScore() > 3);
    }

    @Test
    void lowSideOutlierIsDetectedToo() {
        assertTrue(detector.analyze(normal, 62).anomalous());
    }

    @Test
    void notEnoughHistoryIsNotEvaluated() {
        StatisticalVerdict verdict = detector.analyze(new double[]{70, 71, 69}, 500);
        assertFalse(verdict.anomalous());
        assertEquals(3, verdict.samples());
    }

    @Test
    void constantSignalThatChangesIsAnomalous() {
        double[] flat = {50, 50, 50, 50, 50, 50, 50, 50, 50, 50};
        assertTrue(detector.analyze(flat, 51).anomalous());
        assertFalse(detector.analyze(flat, 50).anomalous());
    }
}
