package org.influxdb.flux;

import org.influxdb.flux.options.FluxConnectionOptions;
import org.influxdb.impl.FluxClientReactiveImpl;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * The Factory that create a reactive instance of a Flux client.
 *
 * @author Jakub Bednar (bednar@github) (26/06/2018 11:13)
 * @since 3.0.0
 */
public final class FluxClientReactiveFactory {

    private FluxClientReactiveFactory() {
    }

    /**
     * Create a instance of the Flux reactive client.
     *
     * @param options the connection configuration
     * @return 3.0.0
     */
    @Nonnull
    public static FluxClientReactive connect(@Nonnull final FluxConnectionOptions options) {

        Objects.requireNonNull(options, "FluxConnectionOptions are required");

        return new FluxClientReactiveImpl(options);
    }
}
