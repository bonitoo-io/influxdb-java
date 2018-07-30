package org.influxdb.flux.operators;

import org.influxdb.flux.Flux;
import org.influxdb.flux.FluxChain;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author Jakub Bednar (bednar@github) (27/06/2018 14:03)
 */
public abstract class AbstractParametrizedFlux extends AbstractFluxWithUpstream {

    protected AbstractParametrizedFlux() {
        super();
    }

    protected AbstractParametrizedFlux(@Nonnull final Flux source) {
        super(source);
    }

    @Override
    protected final void appendAfterUpstream(@Nonnull final FluxChain fluxChain) {

        StringBuilder operator = new StringBuilder();
        //
        // see JoinFlux
        beforeAppendOperatorName(operator, fluxChain);
        //

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
     * For value property it is ": ", but for function it is "=&gt;".
     *
     * @param operatorName operator name
     * @return property value delimiter
     * @see AbstractParametrizedFlux#propertyDelimiter(String)
     */
    @Nonnull
    protected String propertyDelimiter(@Nonnull final String operatorName) {
        return ": ";
    }

    /**
     * Possibility to customize operator.
     *
     * @param operator  current Flux operator
     * @param fluxChain the incoming {@link FluxChain}, never null
     * @see JoinFlux
     */
    protected void beforeAppendOperatorName(@Nonnull final StringBuilder operator, @Nonnull final FluxChain fluxChain) {
    }

    /**
     * @return {@link Boolean#TRUE} if was appended parameter
     */
    private boolean appendParameterTo(@Nonnull final String operatorName,
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
                .append(operatorName)
                .append(propertyDelimiter(operatorName))
                .append(propertyValue);

        return true;
    }
}
