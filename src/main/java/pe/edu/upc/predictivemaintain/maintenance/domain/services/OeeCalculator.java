package pe.edu.upc.predictivemaintain.maintenance.domain.services;

import pe.edu.upc.predictivemaintain.maintenance.domain.model.aggregates.ProductionWindow;

import java.util.List;

/**
 * OEE = Availability x Performance x Quality, computed from production windows.
 * A component that cannot be computed (for example, quality when nothing was produced) is null:
 * it is never replaced by zero or by a made-up value.
 */
public final class OeeCalculator {

    private OeeCalculator() {
    }

    /** Percentages with two decimals; any of them may be null. */
    public record OeeFigures(Double availability, Double performance, Double quality, Double oee) {
    }

    public static OeeFigures compute(List<ProductionWindow> windows) {
        long planned = 0;
        long operating = 0;
        long units = 0;
        long goodUnits = 0;
        double idealSeconds = 0;
        for (ProductionWindow window : windows) {
            planned += window.getPlannedSeconds();
            operating += window.getOperatingSeconds();
            units += window.getTotalUnits();
            goodUnits += window.getGoodUnits();
            idealSeconds += window.getIdealCycleSeconds().doubleValue() * window.getTotalUnits();
        }

        Double availability = planned > 0 ? (double) operating / planned : null;
        Double performance = operating > 0 && units > 0 ? idealSeconds / operating : null;
        Double quality = units > 0 ? (double) goodUnits / units : null;
        Double oee = availability != null && performance != null && quality != null
                ? availability * performance * quality
                : null;
        return new OeeFigures(percent(availability), percent(performance), percent(quality), percent(oee));
    }

    private static Double percent(Double ratio) {
        return ratio == null ? null : Math.round(ratio * 10000.0) / 100.0;
    }
}