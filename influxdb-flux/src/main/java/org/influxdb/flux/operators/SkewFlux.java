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
 *     from(db: "telegraf") |&gt; range(start: -30m, stop: -15m) |&gt; skew()
 * </pre>
 *
 * @author Jakub Bednar (bednar@github) (25/06/2018 10:06)
 * @since 3.0.0
 */
public final class SkewFlux extends AbstractScalarFlux {

    public SkewFlux(@Nonnull final Flux source) {
        super(source);
    }

    public SkewFlux(@Nonnull final Flux source, final boolean useStartTime) {
        super(source, useStartTime);
    }

    public SkewFlux(@Nonnull final Flux source, @Nonnull final String useStartTimeParameter) {
        super(source, useStartTimeParameter);
    }

    @Nonnull
    @Override
    String operatorName() {
        return "skew";
    }
}
