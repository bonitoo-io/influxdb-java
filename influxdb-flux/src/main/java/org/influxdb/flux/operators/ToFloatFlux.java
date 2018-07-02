package org.influxdb.flux.operators;

import org.influxdb.flux.Flux;

import javax.annotation.Nonnull;

/**
 * <a href="https://github.com/influxdata/platform/tree/master/query#tofloat">toFloat</a> - Convert a value to a float.
 *
 * <h3>Example</h3>
 * <pre>
 * Flux flux = Flux
 *     .from("telegraf")
 *     .filter(and(measurement().equal("mem"), field().equal("used")))
 *     .toFloat();
 * </pre>
 *
 * @author Jakub Bednar (bednar@github) (25/06/2018 16:06)
 */
public final class ToFloatFlux extends AbstractParametrizedFlux {

    public ToFloatFlux(@Nonnull final Flux source) {
        super(source);
    }

    @Nonnull
    @Override
    protected String operatorName() {
        return "toFloat";
    }
}
