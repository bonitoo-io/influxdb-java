package org.influxdb.flux.operators;

import org.influxdb.flux.Flux;

import javax.annotation.Nonnull;

/**
 * <a href="https://github.com/influxdata/platform/tree/master/query#skew">skew</a> - Skew of the results.
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
 *     .range(-30L, -15L, ChronoUnit.MINUTES)
 *     .skew();
 * </pre>
 *
 * @author Jakub Bednar (bednar@github) (25/06/2018 10:06)
 * @since 3.0.0
 */
public final class SkewFlux extends AbstractParametrizedFlux {

    public SkewFlux(@Nonnull final Flux source) {
        super(source);
    }

    @Nonnull
    @Override
    String operatorName() {
        return "skew";
    }

    /**
     * @param useStartTime Use the start time as the timestamp of the resulting aggregate
     * @return this
     */
    @Nonnull
    public SkewFlux withUseStartTime(final boolean useStartTime) {

        this.addPropertyValue("useStartTime", useStartTime);

        return this;
    }
}
