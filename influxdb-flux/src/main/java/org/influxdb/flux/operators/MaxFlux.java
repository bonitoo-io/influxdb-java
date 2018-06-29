package org.influxdb.flux.operators;

import org.influxdb.flux.Flux;

import javax.annotation.Nonnull;

/**
 * <a href="https://github.com/influxdata/platform/tree/master/query#max">max</a> - Returns the max value
 * within the results.
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
 *     .range(-12L, ChronoUnit.HOURS)
 *     .window(10L, ChronoUnit.MINUTES)
 *     .max();
 * </pre>
 *
 * @author Jakub Bednar (bednar@github) (25/06/2018 09:49)
 * @since 3.0.0
 */
public final class MaxFlux extends AbstractParametrizedFlux {

    public MaxFlux(@Nonnull final Flux source) {
        super(source);
    }

    @Nonnull
    @Override
    String operatorName() {
        return "max";
    }

    /**
     * @param useStartTime Use the start time as the timestamp of the resulting aggregate
     * @return this
     */
    @Nonnull
    public MaxFlux withUseStartTime(final boolean useStartTime) {

        this.addPropertyValue("useStartTime", useStartTime);

        return this;
    }
}
