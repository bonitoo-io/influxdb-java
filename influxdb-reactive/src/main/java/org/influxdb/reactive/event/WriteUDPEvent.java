package org.influxdb.reactive.event;

import org.influxdb.reactive.option.WriteOptions;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The event is published when the data was written through UDP to InfluxDB server.
 *
 * @author Jakub Bednar (bednar@github) (20/06/2018 07:56)
 * @since 3.0.0
 */
public class WriteUDPEvent extends AbstractWriteEvent {

    private static final Logger LOG = Logger.getLogger(WriteUDPEvent.class.getName());

    public WriteUDPEvent(@Nonnull final List<?> points,
                         @Nonnull final WriteOptions writeOptions) {

        super(points, writeOptions);
    }

    @Override
    protected void logEvent() {
        LOG.log(Level.FINEST, "The data was written through UDP to InfluxDB.");
    }
}
