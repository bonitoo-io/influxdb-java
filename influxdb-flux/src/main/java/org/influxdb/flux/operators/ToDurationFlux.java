package org.influxdb.flux.operators;

import org.influxdb.flux.Flux;

import javax.annotation.Nonnull;

/**
 * <a href="https://github.com/influxdata/platform/tree/master/query#toduration">toDuration</a> -
 * Convert a value to a duration.
 *
 * <h3>Example</h3>
 * <pre>
 * Flux flux = Flux
 *     .from("telegraf")
 *     .filter(and(measurement().equal("mem"), field().equal("used")))
 *     .toDuration();
 * </pre>
 *
 * @author Jakub Bednar (bednar@github) (25/06/2018 16:06)
 */
public final class ToDurationFlux extends AbstractParametrizedFlux {

    public ToDurationFlux(@Nonnull final Flux source) {
        super(source);
    }

    @Nonnull
    @Override
    protected String operatorName() {
        return "toDuration";
    }
}

