package org.influxdb.flux.operators;

import org.influxdb.flux.Flux;
import org.influxdb.flux.FluxChain;

import javax.annotation.Nonnull;

/**
 * @author Jakub Bednar (bednar@github) (25/06/2018 15:54)
 */
abstract class AbstractConvertFlux extends AbstractFluxWithUpstream {

    AbstractConvertFlux(@Nonnull final Flux source) {
        super(source);
    }

    @Override
    void appendAfterUpstream(@Nonnull final FluxChain fluxChain) {

        StringBuilder operator = new StringBuilder();
        //
        // toBool()
        //
        operator.append(operatorName()).append("()");

        fluxChain.append(operator);
    }

    /**
     * @return name of operator
     */
    @Nonnull
    abstract String operatorName();
}
