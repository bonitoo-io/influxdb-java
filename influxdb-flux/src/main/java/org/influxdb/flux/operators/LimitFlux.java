package org.influxdb.flux.operators;

import org.influxdb.flux.Flux;
import org.influxdb.impl.Preconditions;

import javax.annotation.Nonnull;

/**
 * <a href="https://github.com/influxdata/platform/tree/master/query#limit">limit</a> - Restricts the number of rows
 * returned in the results.
 *
 * <h3>Options</h3>
 * <ul>
 * <li><b>n</b> - The maximum number of records to output [int].
 * </ul>
 *
 * <h3>Example</h3>
 * <pre>
 * Flux flux = Flux
 *     .from("telegraf")
 *     .limit(5);
 * </pre>
 *
 * @author Jakub Bednar (bednar@github) (25/06/2018 11:22)
 * @since 3.0.0
 */
public final class LimitFlux extends AbstractParametrizedFlux {

    public LimitFlux(@Nonnull final Flux flux) {
        super(flux);
    }

    @Nonnull
    @Override
    protected String operatorName() {
        return "limit";
    }

    /**
     * @param numberOfResults The number of results
     * @return this
     */
    @Nonnull
    public LimitFlux withN(final int numberOfResults) {

        Preconditions.checkPositiveNumber(numberOfResults, "Number of results");

        this.withPropertyValue("n", numberOfResults);

        return this;
    }
}
