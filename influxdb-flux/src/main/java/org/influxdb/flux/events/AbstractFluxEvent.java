package org.influxdb.flux.events;

import org.influxdb.flux.options.FluxOptions;
import org.influxdb.impl.Preconditions;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Base event triggered by {@link org.influxdb.flux.FluxReactive} client.
 *
 * @author Jakub Bednar (bednar@github) (26/06/2018 13:58)
 */
public abstract class AbstractFluxEvent {

    private final FluxOptions options;
    private final String fluxQuery;

    AbstractFluxEvent(@Nonnull final FluxOptions options, @Nonnull final String fluxQuery) {

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

    /**
     * Log current event by {@link java.util.logging.Logger}.
     */
    public abstract void logEvent();
}
