package org.influxdb.flux;

import org.influxdb.flux.options.FluxConnectionOptions;
import org.influxdb.impl.FluxClientImpl;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * The Factory that create a instance of a Flux client.
 *
 * @author Jakub Bednar (bednar@github) (31/07/2018 13:11)
 * @since 3.0.0
 */
public final class FluxClientFactory {

    private FluxClientFactory() {
    }

    /**
     * Create a instance of the Flux client.
     *
     * @param options the connection configuration
     * @return 3.0.0
     */
    @Nonnull
    public static FluxClient connect(@Nonnull final FluxConnectionOptions options) {

        Objects.requireNonNull(options, "FluxConnectionOptions are required");

        return new FluxClientImpl(options);
    }
}
