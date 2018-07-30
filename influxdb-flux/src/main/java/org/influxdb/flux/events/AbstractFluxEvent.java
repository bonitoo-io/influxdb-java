package org.influxdb.flux.events;

/**
 * Base event triggered by {@code Flux client}.
 *
 * @author Jakub Bednar (bednar@github) (26/06/2018 13:58)
 */
public abstract class AbstractFluxEvent {

    /**
     * Log current event by {@link java.util.logging.Logger}.
     */
    public abstract void logEvent();
}
