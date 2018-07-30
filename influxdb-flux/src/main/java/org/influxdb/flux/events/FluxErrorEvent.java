package org.influxdb.flux.events;

import org.influxdb.InfluxDBException;
import org.influxdb.flux.options.FluxConnectionOptions;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The event is published when arrived the error response from Flux server.
 *
 * @author Jakub Bednar (bednar@github) (26/06/2018 15:35)
 * @since 3.0.0
 */
public class FluxErrorEvent extends AbstractQueryEvent {

    private static final Logger LOG = Logger.getLogger(FluxErrorEvent.class.getName());

    private final InfluxDBException exception;

    public FluxErrorEvent(@Nonnull final FluxConnectionOptions options,
                          @Nonnull final String fluxQuery,
                          @Nonnull final InfluxDBException exception) {

        super(options, fluxQuery);

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
