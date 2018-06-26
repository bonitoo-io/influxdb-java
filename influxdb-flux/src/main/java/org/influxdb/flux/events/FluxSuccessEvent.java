package org.influxdb.flux.events;

import org.influxdb.flux.options.FluxOptions;
import org.influxdb.impl.Preconditions;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The event is published when arrived the success response from Flux server.
 *
 * @author Jakub Bednar (bednar@github) (26/06/2018 13:57)
 * @since 3.0.0
 */
public class FluxSuccessEvent extends AbstractFluxEvent {

    private static final Logger LOG = Logger.getLogger(FluxSuccessEvent.class.getName());

    private final FluxOptions options;
    private final String fluxQuery;

    public FluxSuccessEvent(@Nonnull final FluxOptions options, @Nonnull final String fluxQuery) {

        Objects.requireNonNull(options, "FluxOptions are required");
        Preconditions.checkNonEmptyString(fluxQuery, "Flux query");

        this.options = options;
        this.fluxQuery = fluxQuery;
    }

    /**
     * @return {@link FluxOptions} that was used in query
     */
    @Nonnull
    public FluxOptions getOptions() {
        return options;
    }

    /**
     * @return Flux query sent to Flux server
     */
    @Nonnull
    public String getFluxQuery() {
        return fluxQuery;
    }

    @Override
    public void logEvent() {
        LOG.log(Level.FINEST, "Success response from Flux server.");
    }
}
