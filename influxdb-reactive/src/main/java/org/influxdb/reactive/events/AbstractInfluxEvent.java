package org.influxdb.reactive.events;

/**
 * Base event triggered by InfluxDBReactive client.
 *
 * @author Jakub Bednar (bednar@github) (14/06/2018 08:49)
 * @since 3.0.0
 */
public abstract class AbstractInfluxEvent {

    /**
     * Log current event by {@link java.util.logging.Logger}.
     */
    public abstract void logEvent();
}

