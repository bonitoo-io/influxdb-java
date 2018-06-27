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
public final class LimitFlux extends AbstractParametrizedFlux {

    private final Parameter<Integer> numberOfResults;

    public LimitFlux(@Nonnull final Flux flux) {
        super(flux);

        numberOfResults = null;
    }

    public LimitFlux(@Nonnull final Flux source, @Nonnull final Integer numberOfResults) {
        super(source);

        Preconditions.checkPositiveNumber(numberOfResults, "Number of results");

        this.numberOfResults = (map) -> numberOfResults;
    }

    @Nonnull
    @Override
    String operatorName() {
        return "limit";
    }

    @Nonnull
    @Override
    OperatorParameters getParameters() {

        return OperatorParameters.of("n", numberOfResults);
    }
}
