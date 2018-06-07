package org.influxdb.reactive;

import org.influxdb.InfluxDBException;

import javax.annotation.Nonnull;

/**
 * This listener allow interact with events on {@link org.influxdb.reactive.InfluxDBReactive}.
 *
 * @author Jakub Bednar (bednar@github) (05/06/2018 15:42)
 * @since 3.0.0
 */
public interface InfluxDBReactiveListener {

    /**
     * The method is called when arrived the success response from InfluxDB.
     *
     */
    void doOnSuccessResponse();

    /**
     * The method is called when arrived the error response from InfluxDB.
     *
     * @param throwable propagated InfluxDB exception
     */
    void doOnErrorResponse(@Nonnull final InfluxDBException throwable);

    /**
     * The method is called when occurs a unhandled exception.
     *
     * @param throwable cause
     */
    void doOnError(@Nonnull final Throwable throwable);
}
