package org.combinators.gadget;


import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Describes the properties of a customizable tabletop gadget.
 *
 * Every device has built in capability to execute its operations
 * in a given repeating interval.
 * Supported operations are configured via its <a href="#features">features</a>.
 */
public class Gadget {
    /**
     * Unmodifiable list of all the features of this gadget.
     */
    final List<Feature> features;

    /** Constructs a gadget that does nothing. */
    public Gadget () {
        features = Collections.emptyList();
    }

      /** Constructs a gadget with the given features. */
    protected Gadget(Stream<Feature> features) {
        this.features = Collections.unmodifiableList(features.collect(Collectors.toList()));
    }

    /** Adds a feature. */
    public Gadget add (Feature feature) {
        return new Gadget(Stream.concat(features.stream(), Stream.of(feature)));
    }

    /** Returns all desired features of the given classification. */
    public Stream<Feature> getFeature(FeatureClassification classification) {
        return features.stream()
                .filter(feature -> feature.getClassification() == classification);
    }
}
