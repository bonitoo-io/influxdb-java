package org.influxdb.flux.operators;

import org.influxdb.flux.Flux;

import javax.annotation.Nonnull;

/**
 * <a href="https://github.com/influxdata/platform/tree/master/query#tobool">toBool</a> - Convert a value to a bool.
 *
 * <h3>Example</h3>
 * <pre>
 *     from(db: "telegraf") |&gt; filter(fn:(r) =&gt; r._measurement == "mem" and r._field == "used") |&gt; toBool()
 * </pre>
 *
 * @author Jakub Bednar (bednar@github) (25/06/2018 15:57)
 * @since 3.0.0
 */
public final class ToBoolFlux extends AbstractConvertFlux {

    public ToBoolFlux(@Nonnull final Flux source) {
        super(source);
    }

    @Nonnull
    @Override
    String operatorName() {
        return "toBool";
    }
}
