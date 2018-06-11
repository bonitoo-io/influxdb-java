package org.influxdb.reactive;

import io.reactivex.disposables.Disposable;
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
     * The method is triggered when is Writer subscribed.
     *
     * @param writeDisposable writer disposable
     */
    void doOnSubscribeWriter(@Nonnull final Disposable writeDisposable);

    /**
     * The method is triggered when is backpressure applied.
     *
     * @see io.reactivex.Flowable#onBackpressureBuffer
     */
    void doOnBackpressure();

    /**
     * The method is triggered when arrived the success response from InfluxDB.
     */
    void doOnSuccessResponse();

    /**
     * The method is triggered when arrived the error response from InfluxDB.
     *
     * @param throwable propagated InfluxDB exception
     */
    void doOnErrorResponse(@Nonnull final InfluxDBException throwable);

    /**
     * The method is triggered when occurs a unhandled exception.
     *
     * @param throwable cause
     */
    void doOnError(@Nonnull final Throwable throwable);

    /**
     * The method is triggered when is parsed streamed response to query result.
     */
    void doOnQueryResult();
}
