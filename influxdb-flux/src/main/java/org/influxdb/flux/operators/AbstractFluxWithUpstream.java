package org.influxdb.flux.operators;

import org.influxdb.flux.Flux;
import org.influxdb.flux.FluxChain;
import org.influxdb.flux.Preconditions;

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

    void appendParameterTo(@Nonnull final String parameterName,
                           @Nonnull final FluxChain.FluxParameter parameter,
                           @Nonnull final StringBuilder operator,
                           @Nonnull final FluxChain fluxChain) {

        Preconditions.checkNonEmptyString(parameterName, "Parameter name");
        Objects.requireNonNull(parameter, "FluxParameter is required");
        Objects.requireNonNull(operator, "Current operator");

        if (parameter instanceof FluxChain.NotDefinedParameter) {
            return;
        }

        Object parameterValue = parameter.value(fluxChain.getParameters());

        // n: 5
        operator
                .append(parameterName)
                .append(": ")
                .append(parameterValue.toString());
    }
}
