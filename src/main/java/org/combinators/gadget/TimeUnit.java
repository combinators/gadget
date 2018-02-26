package org.combinators.gadget;

/**
 * Specifies the length of a time interval after which operations recur.
 *
 * Operations cannot be repeated more frequently than once per second.
 */
public enum TimeUnit {
    Second, Minute, Hour, Day, Week, Month, Year
}
