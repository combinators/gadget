package org.combinators.gadget;

/**
 * The feature to record the minimum and maximum values within
 * a given time period.
 *
 * Defaults to daily min/max.
 */
public class ExtremaFeature extends PeriodicFeature implements Feature {
    public final Feature extremaOf;

    public ExtremaFeature(Feature extremaOf) { this (extremaOf, 1, TimeUnit.Day); }
    public ExtremaFeature(Feature extremaOf, TimeUnit unit) { this (extremaOf, 1, unit); }
    public ExtremaFeature(Feature extremaOf, int interval, TimeUnit unit) {
       super (interval, unit);
       this.extremaOf = extremaOf;
    }

    @Override
    public FeatureClassification getClassification() { return FeatureClassification.Extrema; }
}
