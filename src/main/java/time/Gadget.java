package time;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/**
 * Device has built in capability to execute arbitrary operations at a given
 * frequency, as customized by the given parameter.
 */
public class Gadget implements Iterable<Feature> {
    final List<Feature> features = new ArrayList<>();

    /** Default to one-second timer. */
    public Gadget () {
        this (FrequencyUnit.Second);
    }

    /** Defacult to 1 count of the given unit. */
    public Gadget (FrequencyUnit unit) {
        this (1, unit);
    }

    /** Specify exact count of the desired frequency. */
    public Gadget (int count, FrequencyUnit unit) {
        add(new FrequencyFeature(count, FrequencyUnit.Second));
    }

    /** Add a feature to gadget. */
    public Gadget add (Feature f) {
        features.add(f);
        return this;
    }

    /** Return desired feature by Unit, if it exists. */
    public Optional<Feature> getFeature(FeatureUnit unit) {
        for (Feature f: features) {
            if (f.getUnit() == unit) {
                return Optional.of(f);
            }
        }

        return Optional.empty();
    }

    /** Return all known features. */
    public Iterator<Feature> iterator() { return features.iterator(); }
}
