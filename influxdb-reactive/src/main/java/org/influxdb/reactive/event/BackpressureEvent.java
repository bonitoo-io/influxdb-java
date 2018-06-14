package org.influxdb.reactive.event;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The event is published when is backpressure applied.
 *
 * @author Jakub Bednar (bednar@github) (14/06/2018 12:12)
 * @since 3.0.0
 * @see io.reactivex.Flowable#onBackpressureBuffer(int, boolean, boolean, io.reactivex.functions.Action)
 */
public class BackpressureEvent extends AbstractInfluxEvent {

    private static final Logger LOG = Logger.getLogger(BackpressureEvent.class.getName());

    @Override
    protected void logEvent() {
        LOG.log(Level.WARNING, "Backpressure applied, try increase BatchOptionsReactive.bufferLimit");
    }
}
