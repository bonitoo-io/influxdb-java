package org.influxdb.flux.operators;

import org.influxdb.flux.Flux;
import org.influxdb.impl.Preconditions;

import javax.annotation.Nonnull;

/**
 * <a href="https://github.com/influxdata/platform/tree/master/query#set">set</a> - Yield a query results
 * to yielded results.
 *
 * <h3>Options</h3>
 * <ul>
 * <li><b>name</b> - The unique name to give to yielded results [string].</li>
 * </ul>
 *
 * <h3>Example</h3>
 * <pre>
 * Flux flux = Flux
 *     .from("telegraf")
 *     .yield("0");
 * </pre>
 *
 * @author Jakub Bednar (bednar@github) (29/06/2018 09:55)
 * @since 3.0.0
 */
public final class YieldFlux extends AbstractParametrizedFlux {

    public YieldFlux(@Nonnull final Flux source) {
        super(source);
    }

    @Nonnull
    @Override
    String operatorName() {
        return "yield";
    }

    /**
     * @param name The unique name to give to yielded results. Has to be defined.
     * @return this
     */
    @Nonnull
    public final YieldFlux withName(@Nonnull final String name) {
        Preconditions.checkNonEmptyString(name, "Result name");

        withPropertyValueEscaped("name", name);

        return this;
    }
}
