package org.influxdb.flux.operators;

import org.influxdb.flux.Flux;

import javax.annotation.Nonnull;

/**
 * <a href="https://github.com/influxdata/platform/tree/master/query#spread">spread</a> - Difference between min
 * and max values.
 *
 * <h3>Options</h3>
 * <ul>
 * <li><b>useStartTime</b> - Use the start time as the timestamp of the resulting aggregate [boolean]
 * </ul>
 *
 * <h3>Example</h3>
 * <pre>
 *     from(db: "telegraf") |&gt; range(start: -30m) |&gt; spread()
 * </pre>
 *
 *
 * @author Jakub Bednar (bednar@github) (25/06/2018 10:10)
 * @since 3.0.0
 */
public final class SpreadFlux extends AbstractScalarFlux {

    public SpreadFlux(@Nonnull final Flux source) {
        super(source);
    }

    public SpreadFlux(@Nonnull final Flux source, final boolean useStartTime) {
        super(source, useStartTime);
    }

    @Nonnull
    @Override
    String operatorName() {
        return "spread";
    }
}
