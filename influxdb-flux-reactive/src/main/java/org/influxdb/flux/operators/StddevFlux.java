package org.influxdb.flux.operators;

import org.influxdb.flux.Flux;

import javax.annotation.Nonnull;

/**
 * <a href="https://github.com/influxdata/platform/tree/master/query#stddev">stddev</a> - Standard Deviation of
 * the results.
 *
 * <h3>Options</h3>
 * <ul>
 * <li><b>useStartTime</b> - Use the start time as the timestamp of the resulting aggregate [boolean]
 * </ul>
 *
 * <h3>Example</h3>
 * <pre>
 * Flux flux = Flux
 *     .from("telegraf")
 *     .stddev();
 * </pre>
 *
 *
 * @author Jakub Bednar (bednar@github) (25/06/2018 10:15)
 * @since 3.0.0
 */
public final class StddevFlux extends AbstractParametrizedFlux {

    public StddevFlux(@Nonnull final Flux source) {
        super(source);
    }

    @Nonnull
    @Override
    protected String operatorName() {
        return "stddev";
    }

    /**
     * @param useStartTime Use the start time as the timestamp of the resulting aggregate
     * @return this
     */
    @Nonnull
    public StddevFlux withUseStartTime(final boolean useStartTime) {

        this.withPropertyValue("useStartTime", useStartTime);

        return this;
    }
}
