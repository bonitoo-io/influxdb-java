package org.influxdb.impl;

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
    public void doOnError(@Nonnull Throwable throwable) {

        LOG.log(Level.SEVERE, "", throwable);
    }
}