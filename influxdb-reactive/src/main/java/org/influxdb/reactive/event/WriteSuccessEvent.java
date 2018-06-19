package org.influxdb.reactive.event;

import org.influxdb.reactive.option.WriteOptions;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The event is published when arrived the success response from InfluxDB server.
 *
 * @author Jakub Bednar (bednar@github) (14/06/2018 09:14)
 * @since 3.0.0
 */
public class WriteSuccessEvent extends AbstractWriteEvent {

    private static final Logger LOG = Logger.getLogger(WriteSuccessEvent.class.getName());

    public WriteSuccessEvent(@Nonnull final List<?> points,
                             @Nonnull final WriteOptions writeOptions) {

        super(points, writeOptions);
    }

    @Override
    protected void logEvent() {
        LOG.log(Level.FINEST, "Success response from InfluxDB");
    }
}
