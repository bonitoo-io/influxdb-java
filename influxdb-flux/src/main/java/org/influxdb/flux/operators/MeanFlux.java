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
 *     from(db:"foo")
 *          |&gt; filter(fn: (r) =&gt; r["_measurement"] == "mem" AND r["_field"] == "used_percent")
 *          |&gt; range(start:-12h)
 *          |&gt; window(every:10m)
 *          |&gt; mean()
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
}
