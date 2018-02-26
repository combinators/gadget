package org.combinators.gadget;

public class PressureFeature extends PeriodicFeature implements Feature {
    public final String airportCode;

    /** Creates a weather feature with the given properties. */
    public PressureFeature(String airportCode, int interval, TimeUnit unit) {
        super(interval, unit);
        this.airportCode = airportCode;
    }

    /** Creates a weather feature with the given properties, the weather will be checked every hour. */
    public PressureFeature(String airportCode) {
        this(airportCode, 1, TimeUnit.Hour);
    }

    @Override
    public FeatureClassification getClassification() { return FeatureClassification.Weather; }
}
