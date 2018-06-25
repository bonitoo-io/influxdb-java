package org.influxdb.flux.operators;

import org.influxdb.flux.Flux;
import org.influxdb.flux.FluxChain;
import org.influxdb.flux.Preconditions;

import javax.annotation.Nonnull;

/**
 * @author Jakub Bednar (bednar@github) (25/06/2018 09:32)
 */
abstract class AbstractScalarFlux extends AbstractFluxWithUpstream {

    private FluxChain.FluxParameter<Boolean> useStartTime;

    AbstractScalarFlux(@Nonnull final Flux source) {
        super(source);
        this.useStartTime = new FluxChain.NotDefinedParameter<>();
    }

    AbstractScalarFlux(@Nonnull final Flux source, final boolean useStartTime) {
        super(source);
        this.useStartTime = (m) -> useStartTime;
    }

    public AbstractScalarFlux(@Nonnull final Flux source, @Nonnull final String useStartTimeParameter) {
        super(source);

        Preconditions.checkNonEmptyString(useStartTimeParameter, "Use Start Time");

        this.useStartTime = new FluxChain.BoundFluxParameter<>(useStartTimeParameter);
    }

    /**
     * @return name of operator
     */
    @Nonnull
    abstract String operatorName();

    @Override
    protected final void appendAfterUpstream(@Nonnull final FluxChain fluxChain) {

        StringBuilder operator = new StringBuilder();
        //
        // count(
        //
        operator.append(operatorName()).append("(");
        //
        //
        // useStartTime: false
        appendParameters(operator, fluxChain, new NamedParameter("useStartTime", useStartTime));
        //
        // )
        //
        operator.append(")");

        fluxChain.append(operator);
    }
}
