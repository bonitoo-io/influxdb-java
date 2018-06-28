package org.influxdb.flux.operators;

import org.influxdb.flux.Flux;

import javax.annotation.Nonnull;

/**
 * <a href="https://github.com/influxdata/platform/tree/master/query#toint">toInt</a> - Convert a value to a int.
 *
 * <h3>Example</h3>
 * <pre>
 *     from(db: "telegraf") |&gt; filter(fn:(r) =&gt; r._measurement == "mem" and r._field == "used") |&gt; toInt()
 * </pre>
 *
 * @author Jakub Bednar (bednar@github) (25/06/2018 16:06)
 */
public final class ToIntFlux extends AbstractParametrizedFlux {

    public ToIntFlux(@Nonnull final Flux source) {
        super(source);
    }

    @Nonnull
    @Override
    String operatorName() {
        return "toInt";
    }
}
