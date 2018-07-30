package org.influxdb.flux.operators;

import org.influxdb.flux.Flux;

import javax.annotation.Nonnull;

/**
 * <a href="https://github.com/influxdata/platform/tree/master/query#tostring">toString</a> - Convert a value
 * to a string.
 *
 * <h3>Example</h3>
 * <pre>
 *  Flux flux = Flux
 *     .from("telegraf")
 *     .filter(and(measurement().equal("mem"), field().equal("used")))
 *     .toString();
 * </pre>
 *
 * @author Jakub Bednar (bednar@github) (26/06/2018 06:32)
 * @since 3.0.0
 */
public final class ToStringFlux extends AbstractParametrizedFlux {

    public ToStringFlux(@Nonnull final Flux source) {
        super(source);
    }

    @Nonnull
    @Override
    protected String operatorName() {
        return "toString";
    }
}

