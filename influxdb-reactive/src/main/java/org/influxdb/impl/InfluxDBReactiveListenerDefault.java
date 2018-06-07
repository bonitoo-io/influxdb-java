package org.influxdb.impl;

import io.reactivex.disposables.Disposable;
import org.influxdb.InfluxDBException;
import org.influxdb.reactive.InfluxDBReactiveListener;

import javax.annotation.Nonnull;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The listener log events from {@link org.influxdb.reactive.InfluxDBReactive} to {@link #LOG}.
 *
 * @author Jakub Bednar (bednar@github) (05/06/2018 15:46)
 * @since 3.0.0
 */
public class InfluxDBReactiveListenerDefault implements InfluxDBReactiveListener {

    private static final Logger LOG = Logger.getLogger(InfluxDBReactiveListenerDefault.class.getName());

    @Override
    public void doOnSubscribeWriter(@Nonnull final Disposable writeDisposable) {
        LOG.log(Level.FINEST, "Subscribed writer");
    }

    @Override
    public void doOnSuccessResponse() {
        LOG.log(Level.FINEST, "Success response from InfluxDB");
    }

    @Override
    public void doOnErrorResponse(@Nonnull final InfluxDBException throwable) {
        LOG.log(Level.SEVERE, "Error response from InfluxDB: ", throwable);
    }

    @Override
    public void doOnError(@Nonnull final Throwable throwable) {
        LOG.log(Level.SEVERE, "Unexpected error", throwable);
    }

    @Override
    public void doOnBackpressure() {
        LOG.log(Level.WARNING, "Backpressure applied, try increase BatchOptionsReactive.bufferLimit");
    }
}
