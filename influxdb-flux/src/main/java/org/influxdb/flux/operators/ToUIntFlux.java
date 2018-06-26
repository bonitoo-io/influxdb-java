package org.influxdb.flux.operators;

import org.influxdb.flux.Flux;

import javax.annotation.Nonnull;

/**
 * <a href="https://github.com/influxdata/platform/tree/master/query#toduration">toDuration</a> -
 * Convert a value to a duration.
 *
 * <h3>Example</h3>
 * <pre>
 *     from(db: "telegraf") |&gt; filter(fn:(r) =&gt; r._measurement == "mem" and r._field == "used") |&gt; toDuration()
 * </pre>
 *
 * @author Jakub Bednar (bednar@github) (26/06/2018 06:40)
 * @since 3.0.0
 */
public final class ToUIntFlux extends AbstractConvertFlux {

    public ToUIntFlux(@Nonnull final Flux source) {
        super(source);
    }

    @Nonnull
    @Override
    String operatorName() {
        return "toUInt";
    }
}
