package org.influxdb.flux.operators;

import org.influxdb.flux.Flux;

import javax.annotation.Nonnull;

/**
 * <a href="https://github.com/influxdata/platform/tree/master/query#range">range</a> - Filters the results by
 * time boundaries.
 *
 * <h3>Options</h3>
 * <ul>
 * <li><b>start</b> - Specifies the oldest time to be included in the results [duration or timestamp]</li>
 * <li>
 * <b>stop</b> - Specifies the exclusive newest time to be included in the results.
 * Defaults to "now". [duration or timestamp]
 * </li>
 * </ul>
 *
 * <h3>Example</h3>
 * <pre>
 *     from(db:"foo")
 *          |&gt; filter(fn: (r) =&gt; r["_measurement"] == "cpu" AND
 *                    r["_field"] == "usage_system")
 *          |&gt; range(start:-12h, stop: -15m)
 *
 *    from(db:"foo")
 *          |&gt; range(start: 2018-05-23T13:09:22.885021542Z)
 *          |&gt; derivative(unit:100ms)
 * </pre>
 *
 * @author Jakub Bednar (bednar@github) (26/06/2018 07:04)
 * @since 3.0.0
 */
public final class RangeFlux extends AbstractParametrizedFlux {

    public RangeFlux(@Nonnull final Flux flux) {
        super(flux);
    }

    @Nonnull
    @Override
    String operatorName() {
        return "range";
    }
}
