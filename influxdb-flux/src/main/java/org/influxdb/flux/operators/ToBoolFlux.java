package org.influxdb.flux.operators;

import org.influxdb.flux.Flux;

import javax.annotation.Nonnull;

/**
 * <a href="https://github.com/influxdata/platform/tree/master/query#tobool">toBool</a> - Convert a value to a bool.
 *
 * <h3>Example</h3>
 * <pre>
 * Flux flux = Flux
 *     .from("telegraf")
 *     .filter(and(measurement().equal("mem"), field().equal("used")))
 *     .toBool();
 * </pre>
 *
 * @author Jakub Bednar (bednar@github) (25/06/2018 15:57)
 * @since 3.0.0
 */
public final class ToBoolFlux extends AbstractParametrizedFlux {

    public ToBoolFlux(@Nonnull final Flux source) {
        super(source);
    }

    @Nonnull
    @Override
    protected String operatorName() {
        return "toBool";
    }
}
