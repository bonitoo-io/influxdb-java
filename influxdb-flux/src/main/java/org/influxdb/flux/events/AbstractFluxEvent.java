package org.influxdb.flux.events;

import org.influxdb.flux.options.FluxConnectionOptions;
import org.influxdb.impl.Preconditions;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Base event triggered by {@code Flux client}.
 *
 * @author Jakub Bednar (bednar@github) (26/06/2018 13:58)
 */
public abstract class AbstractFluxEvent {

    private final FluxConnectionOptions options;
    private final String fluxQuery;

    AbstractFluxEvent(@Nonnull final FluxConnectionOptions options, @Nonnull final String fluxQuery) {

        Objects.requireNonNull(options, "FluxConnectionOptions are required");
        Preconditions.checkNonEmptyString(fluxQuery, "Flux query");

        this.options = options;
        this.fluxQuery = fluxQuery;
    }

    /**
     * @return {@link FluxConnectionOptions} that was used in query
     */
    @Nonnull
    public FluxConnectionOptions getOptions() {
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
