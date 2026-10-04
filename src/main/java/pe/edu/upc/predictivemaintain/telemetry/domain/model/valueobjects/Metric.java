package pe.edu.upc.predictivemaintain.telemetry.domain.model.valueobjects;

/**
 * Physical variables the platform can monitor (the list of SP-01: vibration, temperature, pressure,
 * amperage, noise). A sensor with any other metric is rejected (US-11, scenario 2).
 */
public enum Metric {
    VIBRATION,
    TEMPERATURE,
    PRESSURE,
    CURRENT,
    NOISE
}