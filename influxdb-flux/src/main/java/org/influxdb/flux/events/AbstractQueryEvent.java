package org.influxdb.flux.events;

import org.influxdb.flux.options.FluxConnectionOptions;
import org.influxdb.impl.Preconditions;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * @author Jakub Bednar (bednar@github) (30/07/2018 14:59)
 */
public abstract class AbstractQueryEvent extends AbstractFluxEvent {

    private final FluxConnectionOptions options;
    private final String fluxQuery;

    AbstractQueryEvent(@Nonnull final FluxConnectionOptions options, @Nonnull final String fluxQuery) {

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
}
