package org.influxdb.flux.operators;

import org.influxdb.flux.Flux;

import javax.annotation.Nonnull;

/**
 * <a href="https://github.com/influxdata/platform/tree/master/query#last">last</a> - Returns the last result of
 * the query.
 *
 * <h3>Options</h3>
 * <ul>
 * <li><b>useStartTime</b> - Use the start time as the timestamp of the resulting aggregate [boolean]
 * </ul>
 *
 * <h3>Example</h3>
 * <pre>
 *     from(db:"telegraf") |&gt; last()
 * </pre>
 *
 * @author Jakub Bednar (bednar@github) (25/06/2018 09:43)
 * @since 3.0.0
 */
public final class LastFlux extends AbstractScalarFlux {

    public LastFlux(@Nonnull final Flux source) {
        super(source);
    }

    public LastFlux(@Nonnull final Flux source, final boolean useStartTime) {
        super(source, useStartTime);
    }

    @Nonnull
    @Override
    String operatorName() {
        return "last";
    }
}
