package org.influxdb.flux;

import org.influxdb.flux.impl.FluxReactiveImpl;
import org.influxdb.flux.options.FluxOptions;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * The Factory that create a reactive instance of a Flux client.
 *
 * @author Jakub Bednar (bednar@github) (26/06/2018 11:13)
 * @since 3.0.0
 */
public final class FluxReactiveFactory {

    private FluxReactiveFactory() {
    }

    /**
     * Create a instance of the Flux reactive client.
     *
     * @param options the connection configuration
     * @return 3.0.0
     */
    @Nonnull
    public static FluxReactive connect(@Nonnull final FluxOptions options) {

        Objects.requireNonNull(options, "FluxOptions are required");

        return new FluxReactiveImpl(options);
    }
}
