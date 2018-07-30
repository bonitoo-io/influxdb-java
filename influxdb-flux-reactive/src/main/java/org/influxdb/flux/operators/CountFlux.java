package org.influxdb.flux.operators;

import org.influxdb.flux.Flux;

import javax.annotation.Nonnull;

/**
 * <a href="https://github.com/influxdata/platform/tree/master/query#count">count</a> - Counts the number of results.
 *
 * <h3>Options</h3>
 * <ul>
 * <li><b>useStartTime</b> - Use the start time as the timestamp of the resulting aggregate [boolean]
 * </ul>
 *
 * <h3>Example</h3>
 * <pre>
 * Flux flux = Flux
 *      .from("telegraf")
 *      .count();
 * </pre>
 *
 * @author Jakub Bednar (bednar@github) (22/06/2018 11:06)
 * @since 3.0.0
 */
public final class CountFlux extends AbstractParametrizedFlux {

    public CountFlux(@Nonnull final Flux source) {
        super(source);
    }

    @Nonnull
    @Override
    protected String operatorName() {
        return "count";
    }

    /**
     * @param useStartTime Use the start time as the timestamp of the resulting aggregate
     * @return this
     */
    @Nonnull
    public CountFlux withUseStartTime(final boolean useStartTime) {

        this.withPropertyValue("useStartTime", useStartTime);

        return this;
    }
}
