package org.influxdb.reactive;

import retrofit2.Response;

import javax.annotation.Nonnull;

/**
 * This listener allow interact with events on {@link org.influxdb.reactive.InfluxDBReactive}.
 *
 * @author Jakub Bednar (bednar@github) (05/06/2018 15:42)
 * @since 3.0.0
 */
public interface InfluxDBReactiveListener {

    /**
     * The method is called when arrived the response from InfluxDB.
     *
     * @param response the InfluxDB response
     */
    void doOnResponse(@Nonnull final Response<String> response);

    /**
     * The method is called when occurs a unhandled exception.
     *
     * @param throwable cause
     */
    void doOnError(@Nonnull final Throwable throwable);
}
