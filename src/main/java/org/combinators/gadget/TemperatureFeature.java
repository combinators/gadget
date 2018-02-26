package org.combinators.gadget;

/**
 * The feature to retrieve weather information.
 *
 * Uses an airport code to determine the location.
 * Customized to also report weather values using designated TemperatureUnit.
 */
public class TemperatureFeature extends PeriodicFeature implements Feature {
    public final String airportCode;
    public final TemperatureUnit temperatureUnit;

    /** Creates a weather feature with the given properties. */
    public TemperatureFeature(String airportCode, TemperatureUnit temperatureUnit, int interval, TimeUnit unit) {
        super(interval, unit);
        this.airportCode = airportCode;
        this.temperatureUnit = temperatureUnit;
    }

    /** Creates a weather feature with the given properties, the weather will be checked every hour. */
    public TemperatureFeature(String airportCode, TemperatureUnit temperatureUnit) {
        this(airportCode, temperatureUnit, 1, TimeUnit.Hour);
    }

    @Override
    public FeatureClassification getClassification() { return FeatureClassification.Weather; }
}
