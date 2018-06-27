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
 *     from(db:"telegraf") |&gt; count()
 * </pre>
 *
 * @author Jakub Bednar (bednar@github) (22/06/2018 11:06)
 * @since 3.0.0
 */
public final class CountFlux extends AbstractScalarFlux {

    public CountFlux(@Nonnull final Flux source) {
        super(source);
    }

    public CountFlux(@Nonnull final Flux source, final boolean useStartTime) {
        super(source, useStartTime);
    }

    @Nonnull
    @Override
    String operatorName() {
        return "count";
    }
}
