package org.influxdb.flux.operators;

import org.influxdb.flux.Flux;
import org.influxdb.impl.Preconditions;

import javax.annotation.Nonnull;

/**
 * @author Jakub Bednar (bednar@github) (25/06/2018 09:32)
 */
abstract class AbstractScalarFlux extends AbstractParametrizedFlux {

    private Parameter<Boolean> useStartTime;

    AbstractScalarFlux(@Nonnull final Flux source) {
        super(source);
        this.useStartTime = new NotDefinedParameter<>();
    }

    AbstractScalarFlux(@Nonnull final Flux source, final boolean useStartTime) {
        super(source);
        this.useStartTime = (m) -> useStartTime;
    }

    public AbstractScalarFlux(@Nonnull final Flux source, @Nonnull final String useStartTimeParameter) {
        super(source);

        Preconditions.checkNonEmptyString(useStartTimeParameter, "Use Start Time");

        this.useStartTime = new BoundParameter<>(useStartTimeParameter);
    }

    @Nonnull
    @Override
    OperatorParameters getParameters() {

        return OperatorParameters.of("useStartTime", useStartTime);
    }
}
