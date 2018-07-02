package org.influxdb.flux.custom;

import org.influxdb.flux.Flux;
import org.influxdb.flux.operators.AbstractParametrizedFlux;
import org.influxdb.impl.Preconditions;

import javax.annotation.Nonnull;

/**
 * The custom function.
 *
 * @author Jakub Bednar (bednar@github) (02/07/2018 14:23)
 */
public class FilterMeasurement extends AbstractParametrizedFlux {

    public FilterMeasurement(@Nonnull final Flux source) {
        super(source);
    }

    @Nonnull
    @Override
    protected String operatorName() {
        return "measurement";
    }

    /**
     * @param measurement the measurement name. Has to be defined.
     * @return this
     */
    @Nonnull
    public FilterMeasurement withName(@Nonnull final String measurement) {

        Preconditions.checkNonEmptyString(measurement, "Measurement name");

        withPropertyValueEscaped("m", measurement);

        return this;
    }
}