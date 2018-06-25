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
 *     from(db:"foo")
 *          |&gt; filter(fn: (r) =&gt; r["_measurement"]=="cpu" AND
 *                 r["_field"] == "usage_system" AND
 *                 r["service"] == "app-server")
 *          |&gt; range(start:-12h)
 *          |&gt; window(every:10m)
 *          |&gt; max()
 * </pre>
 *
 * @author Jakub Bednar (bednar@github) (25/06/2018 09:49)
 * @since 3.0.0
 */
public final class MaxFlux extends AbstractScalarFlux {

    public MaxFlux(@Nonnull final Flux source) {
        super(source);
    }

    public MaxFlux(@Nonnull final Flux source, final boolean useStartTime) {
        super(source, useStartTime);
    }

    @Nonnull
    @Override
    String operatorName() {
        return "max";
    }
}
