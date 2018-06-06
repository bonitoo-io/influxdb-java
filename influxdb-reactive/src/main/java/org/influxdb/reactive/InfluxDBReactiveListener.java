package org.influxdb.reactive;

import javax.annotation.Nonnull;

/**
 * This listener allow interact with events on {@link org.influxdb.impl.InfluxDBServiceReactive}.
 *
 * @author Jakub Bednar (bednar@github) (05/06/2018 15:42)
 * @since 3.0.0
 */
public interface InfluxDBReactiveListener {

    /**
     * The method is called when occurs a unhandled exception.
     *
     * @param throwable cause
     */
    void doOnError(@Nonnull final Throwable throwable);
}