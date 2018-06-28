package org.influxdb.flux.operators;

import org.influxdb.flux.Flux;

import javax.annotation.Nonnull;

/**
 * <a href="https://github.com/influxdata/platform/tree/master/query#totime">toTime</a> - Convert a value to a time.
 *
 * <h3>Example</h3>
 * <pre>
 *     from(db: "telegraf") |&gt; filter(fn:(r) =&gt; r._measurement == "mem" and r._field == "used") |&gt; toTime()
 * </pre>
 *
 * @author Jakub Bednar (bednar@github) (26/06/2018 06:37)
 * @since 3.0.0
 */
public final class ToTimeFlux extends AbstractParametrizedFlux {

    public ToTimeFlux(@Nonnull final Flux source) {
        super(source);
    }

    @Nonnull
    @Override
    String operatorName() {
        return "toTime";
    }
}
