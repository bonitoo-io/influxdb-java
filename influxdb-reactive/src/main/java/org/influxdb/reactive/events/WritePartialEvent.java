package org.influxdb.reactive.events;

import org.influxdb.InfluxDBException;
import org.influxdb.reactive.options.WriteOptions;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The event is published when arrived the partial error response from InfluxDB server.
 *
 * @author Jakub Bednar (bednar@github) (15/06/2018 12:49) zaz
 */
public class WritePartialEvent extends AbstractWriteEvent {

    private static final Logger LOG = Logger.getLogger(WritePartialEvent.class.getName());

    private final InfluxDBException.PartialWriteException exception;

    public WritePartialEvent(@Nonnull final List<?> points,
                             @Nonnull final WriteOptions writeOptions,
                             @Nonnull final InfluxDBException.PartialWriteException exception) {

        super(points, writeOptions);

        Objects.requireNonNull(exception, "InfluxDBException is required");

        this.exception = exception;
    }

    /**
     * @return the partial exception that was throw
     */
    @Nonnull
    public InfluxDBException.PartialWriteException getException() {
        return exception;
    }

    @Override
    public void logEvent() {
        LOG.log(Level.FINEST, "Success response from InfluxDB");
    }
}
