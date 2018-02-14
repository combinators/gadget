package time;

/**
 * Designated capability to record the minimum and maximum values within
 * a given time period.
 *
 * Defaults to daily min/max
 */
public class ExtremaFeature extends FrequencyFeature {
    public ExtremaFeature() { super (1, FrequencyUnit.Day); }
    public ExtremaFeature(FrequencyUnit unit) { this (1, unit); }

    public ExtremaFeature(int count, FrequencyUnit unit) {
       super (count, unit);
    }

    @Override
    public FeatureUnit getUnit() { return FeatureUnit.Extrema; }

}
