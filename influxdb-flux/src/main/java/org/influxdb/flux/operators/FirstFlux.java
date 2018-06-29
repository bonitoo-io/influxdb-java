package org.influxdb.flux.operators;

import org.influxdb.flux.Flux;

import javax.annotation.Nonnull;

/**
 * <a href="https://github.com/influxdata/platform/tree/master/query#first">first</a> - Returns the first result of
 * the query.
 *
 * <h3>Options</h3>
 * <ul>
 * <li><b>useStartTime</b> - Use the start time as the timestamp of the resulting aggregate [boolean]
 * </ul>
 *
 * <h3>Example</h3>
 * <pre>
 * Flux flux = Flux
 *     .from("telegraf")
 *     .first();
 * </pre>
 *
 * @author Jakub Bednar (bednar@github) (25/06/2018 09:28)
 * @since 3.0.0
 */
public final class FirstFlux extends AbstractParametrizedFlux {

    public FirstFlux(@Nonnull final Flux source) {
        super(source);
    }

    @Nonnull
    @Override
    String operatorName() {
        return "first";
    }
}
