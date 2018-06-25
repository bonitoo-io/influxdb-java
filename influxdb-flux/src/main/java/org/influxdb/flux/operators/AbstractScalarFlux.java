package org.influxdb.flux.operators;

import org.influxdb.flux.Flux;
import org.influxdb.flux.FluxChain;

import javax.annotation.Nonnull;

/**
 * @author Jakub Bednar (bednar@github) (25/06/2018 09:32)
 */
abstract class AbstractScalarFlux extends AbstractFluxWithUpstream {

    private Boolean useStartTime;

    AbstractScalarFlux(@Nonnull final Flux source) {
        super(source);
    }

    AbstractScalarFlux(@Nonnull final Flux source, final boolean useStartTime) {
        super(source);
        this.useStartTime = useStartTime;
    }

    /**
     * @return name of operator
     */
    @Nonnull
    abstract String operatorName();

    @Override
    protected final void appendAfterUpstream(@Nonnull final FluxChain fluxChain) {

        StringBuilder count = new StringBuilder();
        //
        // count(
        //
        count.append(operatorName()).append("(");
        //
        //
        // useStartTime: false
        if (useStartTime != null) {
            count.append("useStartTime: ").append(useStartTime.toString().toLowerCase());
        }
        //
        // )
        //
        count.append(")");

        fluxChain.append(count);
    }
}
