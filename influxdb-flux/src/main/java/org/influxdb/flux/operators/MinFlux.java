package org.influxdb.flux.operators;

import org.influxdb.flux.Flux;

import javax.annotation.Nonnull;

/**
 * <a href="https://github.com/influxdata/platform/tree/master/query#min">min</a> - Returns the min value within
 * the results.
 *
 * <h3>Options</h3>
 * <ul>
 * <li><b>useStartTime</b> - Use the start time as the timestamp of the resulting aggregate [boolean]
 * </ul>
 *
 * <h3>Example</h3>
 * <pre>
 *     from(db:"foo")
 *          |&gt; filter(fn: (r) =&gt; r[ "_measurement"] == "cpu" AND r["_field" ]== "usage_system")
 *          |&gt; range(start:-12h)
 *          |&gt; window(every:10m, period: 5m)
 *          |&gt; min()
 * </pre>
 *
 * @author Jakub Bednar (bednar@github) (25/06/2018 10:00)
 * @since 3.0.0
 */
public final class MinFlux extends AbstractScalarFlux {

    public MinFlux(@Nonnull final Flux source) {
        super(source);
    }

    public MinFlux(@Nonnull final Flux source, final boolean useStartTime) {
        super(source, useStartTime);
    }

    @Nonnull
    @Override
    String operatorName() {
        return "min";
    }
}
