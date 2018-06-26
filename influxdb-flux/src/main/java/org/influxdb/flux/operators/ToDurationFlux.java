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
 * @author Jakub Bednar (bednar@github) (25/06/2018 16:06)
 */
public final class ToDurationFlux extends AbstractConvertFlux {

    public ToDurationFlux(@Nonnull final Flux source) {
        super(source);
    }

    @Nonnull
    @Override
    String operatorName() {
        return "toDuration";
    }
}

