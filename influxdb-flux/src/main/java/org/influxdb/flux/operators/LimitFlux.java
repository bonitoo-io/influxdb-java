package org.influxdb.flux.operators;

import org.influxdb.flux.Flux;
import org.influxdb.flux.FluxChain;
import org.influxdb.flux.FluxChain.FluxParameter;
import org.influxdb.flux.Preconditions;

import javax.annotation.Nonnull;

/**
 * <a href="https://github.com/influxdata/platform/tree/master/query#limit">limit</a> - Restricts the number of rows
 * returned in the results.
 *
 * <h3>Options</h3>
 * <ul>
 * <li><b>n</b> - The number of results.
 * </ul>
 *
 * <h3>Example</h3>
 * <pre>
 *     from(db: "telegraf") |&gt; limit(n: 10)
 * </pre>
 *
 * @author Jakub Bednar (bednar@github) (25/06/2018 11:22)
 * @since 3.0.0
 */
public final class LimitFlux extends AbstractFluxWithUpstream {

    private final FluxParameter<Integer> numberOfResults;

    public LimitFlux(@Nonnull final Flux source, @Nonnull final Integer numberOfResults) {
        super(source);

        Preconditions.checkPositiveNumber(numberOfResults, "Number of results");

        this.numberOfResults = (map) -> numberOfResults;
    }

    public LimitFlux(@Nonnull final Flux source, @Nonnull final String numberOfResultsParameter) {
        super(source);

        Preconditions.checkNonEmptyString(numberOfResultsParameter, "Number of results");

        this.numberOfResults = new FluxChain.BoundFluxParameter<>(numberOfResultsParameter);
    }

    @Override
    void appendAfterUpstream(@Nonnull final FluxChain fluxChain) {

        StringBuilder limit = new StringBuilder();
        //
        // limit(
        //
        limit.append("limit(");
        //
        //
        // n: 5
        appendParameters(limit, fluxChain, new NamedParameter("n", numberOfResults));
        //
        // )
        //
        limit.append(")");

        fluxChain.append(limit);
    }
}
