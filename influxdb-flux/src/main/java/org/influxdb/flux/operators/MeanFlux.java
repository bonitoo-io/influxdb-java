package org.influxdb.flux.operators;

import org.influxdb.flux.Flux;

import javax.annotation.Nonnull;

/**
 * <a href="https://github.com/influxdata/platform/tree/master/query#mean">mean</a> - Returns the mean of the
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
 *     .mean();
 * </pre>
 *
 * @author Jakub Bednar (bednar@github) (25/06/2018 09:55)
 * @since 3.0.0
 */
public final class MeanFlux extends AbstractParametrizedFlux {

    public MeanFlux(@Nonnull final Flux source) {
        super(source);
    }

    @Nonnull
    @Override
    String operatorName() {
        return "mean";
    }

    /**
     * @param useStartTime Use the start time as the timestamp of the resulting aggregate
     * @return this
     */
    @Nonnull
    public MeanFlux withUseStartTime(final boolean useStartTime) {

        this.addPropertyValue("useStartTime", useStartTime);

        return this;
    }
}
