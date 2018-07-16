package org.influxdb.reactive.events;

import org.influxdb.InfluxDBException;
import org.influxdb.reactive.options.WriteOptions;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The event is published when arrived the error response from InfluxDB server.
 *
 * @author Jakub Bednar (bednar@github) (14/06/2018 10:16)
 */
public class WriteErrorEvent extends AbstractWriteEvent {

    private static final Logger LOG = Logger.getLogger(WriteErrorEvent.class.getName());

    private final InfluxDBException exception;

    public WriteErrorEvent(@Nonnull final List<?> dataPoints,
                           @Nonnull final WriteOptions writeOptions,
                           @Nonnull final InfluxDBException exception) {

        super(dataPoints, writeOptions);

        Objects.requireNonNull(dataPoints, "Points are required");
        Objects.requireNonNull(writeOptions, "WriteOptions are required");
        Objects.requireNonNull(exception, "InfluxDBException is required");

        this.exception = exception;
    }

    /**
     * @return the exception that was throw
     */
    @Nonnull
    public InfluxDBException getException() {
        return exception;
    }

    @Override
    public void logEvent() {

        LOG.log(Level.SEVERE, "Error response from InfluxDB: ", exception);
    }
}
