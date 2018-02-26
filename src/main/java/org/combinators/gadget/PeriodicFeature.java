package org.combinators.gadget;


 /**
  * The feature to repeat all gadget operations in a specified interval.
  */
public abstract class PeriodicFeature {
    public final TimeUnit unit;
    public final int interval;

    /** Constructs a PeriodicFeature operating at a unit interval. */
    public PeriodicFeature(TimeUnit unit) { this (1, unit); }

    /** Constructs a PeriodicFeature operating at the specified interval. */
    public PeriodicFeature(int interval, TimeUnit unit) {
        this.unit = unit;
        this.interval = interval;
    }
}
