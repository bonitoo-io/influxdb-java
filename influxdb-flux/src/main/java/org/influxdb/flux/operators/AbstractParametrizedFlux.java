package org.influxdb.flux.operators;

import org.influxdb.flux.Flux;
import org.influxdb.flux.FluxChain;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author Jakub Bednar (bednar@github) (27/06/2018 14:03)
 */
public abstract class AbstractParametrizedFlux extends AbstractFluxWithUpstream {

    protected AbstractParametrizedFlux(@Nonnull final Flux source) {
        super(source);
    }

    @Override
    protected final void appendAfterUpstream(@Nonnull final FluxChain fluxChain) {

        StringBuilder operator = new StringBuilder();
        //
        // operator(
        //
        operator.append(operatorName()).append("(");
        //
        //
        // parameters: false
        boolean wasAppended = false;

        for (String name : operatorProperties.keys()) {

            String propertyValue = operatorProperties.get(name, fluxChain.getParameters());

            wasAppended = appendParameterTo(name, propertyValue, operator, wasAppended);
        }
        //
        // )
        //
        operator.append(")");

        fluxChain.append(operator);
    }

    /**
     * @return name of operator
     */
    @Nonnull
    protected abstract String operatorName();

    /**
     * @return {@link Boolean#TRUE} if was appended parameter
     */
    private boolean appendParameterTo(@Nonnull final String propertyName,
                                      @Nullable final String propertyValue,
                                      @Nonnull final StringBuilder operator,
                                      final boolean wasAppendProperty) {

        if (propertyValue == null) {
            return wasAppendProperty;
        }

        // delimit previously appended parameter
        if (wasAppendProperty) {
            operator.append(", ");
        }

        // n: 5
        operator
                .append(propertyName)
                .append(": ")
                .append(propertyValue);

        return true;
    }

}
