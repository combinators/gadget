package time;

/**
 * Designated capability to activate logic at given frequency
 */
public class FrequencyFeature implements Feature {
    public final FrequencyUnit unit;
    public final int count;

    @Override
    public FeatureUnit getUnit() { return FeatureUnit.Frequency; }

    public FrequencyFeature(FrequencyUnit unit) { this (1, unit); }

    public FrequencyFeature(int count, FrequencyUnit unit) {
        this.unit = unit;
        this.count = count;
    }
}
