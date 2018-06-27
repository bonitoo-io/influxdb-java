package org.influxdb.flux.operators;

import org.influxdb.flux.Flux;
import org.influxdb.flux.FluxChain;
import org.influxdb.impl.Preconditions;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Jakub Bednar (bednar@github) (25/06/2018 09:32)
 */
abstract class AbstractScalarFlux extends AbstractParametrizedFlux {

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

    @Nonnull
    @Override
    List<NamedParameter> getParameters() {

        List<NamedParameter> namedParameters = new ArrayList<>();
        namedParameters.add(new NamedParameter("useStartTime", useStartTime));

        return namedParameters;
    }
}
