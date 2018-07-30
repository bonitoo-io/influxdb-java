package org.influxdb.flux.events;

import org.influxdb.flux.options.FluxConnectionOptions;

import javax.annotation.Nonnull;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The event is published when arrived the success response from Flux server.
 *
 * @author Jakub Bednar (bednar@github) (26/06/2018 13:57)
 * @since 3.0.0
 */
public class FluxSuccessEvent extends AbstractQueryEvent {

    private static final Logger LOG = Logger.getLogger(FluxSuccessEvent.class.getName());

    public FluxSuccessEvent(@Nonnull final FluxConnectionOptions options, @Nonnull final String fluxQuery) {

        super(options, fluxQuery);
    }

    @Override
    public void logEvent() {
        LOG.log(Level.FINEST, "Success response from Flux server.");
    }
}
