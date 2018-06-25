package org.influxdb.flux.operators;

import org.influxdb.flux.Flux;
import org.influxdb.flux.FluxChain;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Abstract base class for operators that take an upstream source of {@link Flux}.
 *
 * @author Jakub Bednar (bednar@github) (25/06/2018 07:29)
 */
abstract class AbstractFluxWithUpstream extends Flux {

    Flux source;

    AbstractFluxWithUpstream(@Nonnull final Flux source) {

        Objects.requireNonNull(source, "Source is required");

        this.source = source;
    }

    @Override
    protected final void appendActual(@Nonnull final FluxChain fluxChain) {
        fluxChain.append(source);

        appendAfterUpstream(fluxChain);
    }

    /**
     * Append the actual operator to {@link FluxChain}.
     *
     * @param fluxChain the incoming {@link FluxChain}, never null
     */
    abstract void appendAfterUpstream(@Nonnull final FluxChain fluxChain);
}
