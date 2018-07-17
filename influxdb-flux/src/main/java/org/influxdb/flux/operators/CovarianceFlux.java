package org.influxdb.flux.operators;

import org.influxdb.flux.Flux;
import org.influxdb.impl.Preconditions;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Objects;

/**
 * <a href="https://github.com/influxdata/platform/blob/master/query/docs/SPEC.md#covariance">covariance</a> -
 * Covariance is an aggregate operation. Covariance computes the covariance between two columns.
 *
 * <h3>Options</h3>
 * <ul>
 * <li>
 * <b>columns</b> - List of columns on which to compute the covariance.
 * Exactly two columns must be provided [array of strings].
 * </li>
 * <li>
 * <b>pearsonr</b> - Indicates whether the result should be normalized to be the
 * <a href="https://en.wikipedia.org/wiki/Pearson_correlation_coefficient">Pearson R coefficient</a> [boolean].
 * </li>
 * <li><b>valueDst</b> - The column into which the result will be placed. Defaults to <i>_value`</i> [string].</li>
 * </ul>
 *
 * <h3>Example</h3>
 * <pre>
 * Flux flux = Flux
 *     .from("telegraf")
 *     .covariance(new String[]{"_value", "_valueSquare"});
 * </pre>
 *
 * @author Jakub Bednar (bednar@github) (17/07/2018 13:13)
 * @since 3.0.0
 */
public final class CovarianceFlux extends AbstractParametrizedFlux {

    public CovarianceFlux(@Nonnull final Flux source) {
        super(source);
    }

    @Nonnull
    @Override
    protected String operatorName() {
        return "covariance";
    }

    /**
     * @param columns list of columns on which to compute the covariance. Exactly two columns must be provided.
     * @return this
     */
    @Nonnull
    public CovarianceFlux withColumns(@Nonnull final String[] columns) {

        Objects.requireNonNull(columns, "Columns are required");

        if (columns.length != 2) {
            throw new IllegalArgumentException("Exactly two columns must be provided.");
        }

        this.withPropertyValue("columns", columns);

        return this;
    }

    /**
     * @param columns list of columns on which to compute the covariance. Exactly two columns must be provided.
     * @return this
     */
    @Nonnull
    public CovarianceFlux withColumns(@Nonnull final Collection<String> columns) {

        Objects.requireNonNull(columns, "Columns are required");

        if (columns.size() != 2) {
            throw new IllegalArgumentException("Exactly two columns must be provided.");
        }

        this.withPropertyValue("columns", columns);

        return this;
    }

    /**
     * @param pearsonr Indicates whether the result should be normalized to be the Pearson R coefficient
     * @return this
     */
    @Nonnull
    public CovarianceFlux withPearsonr(final boolean pearsonr) {

        this.withPropertyValue("pearsonr", pearsonr);

        return this;
    }

    /**
     * @param valueDst column into which the result will be placed.
     * @return this
     */
    @Nonnull
    public CovarianceFlux withValueDst(@Nonnull final String valueDst) {

        Preconditions.checkNonEmptyString(valueDst, "Value destination");

        this.withPropertyValueEscaped("valueDst", valueDst);

        return this;
    }
}
