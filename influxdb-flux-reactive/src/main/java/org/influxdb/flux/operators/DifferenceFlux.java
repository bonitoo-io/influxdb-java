package org.influxdb.flux.operators;

import org.influxdb.flux.Flux;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Objects;

/**
 * <a href="https://github.com/influxdata/platform/blob/master/query/docs/SPEC.md#difference">difference</a> -
 * Difference computes the difference between subsequent non null records.
 *
 * <h3>Options</h3>
 * <ul>
 * <li>
 * <b>nonNegative</b> - Indicates if the derivative is allowed to be negative.
 * If a value is encountered which is less than the previous value
 * then it is assumed the previous value should have been a zero [boolean].
 * </li>
 * <li>
 * <b>columns</b> - The list of columns on which to compute the difference.
 * Defaults <i>["_value"]</i> [array of strings].
 * </li>
 * </ul>
 *
 * <h3>Example</h3>
 * <pre>
 * Flux flux = Flux
 *     .from("telegraf")
 *     .groupBy("_measurement")
 *     .difference();
 *
 * Flux flux = Flux
 *     .from("telegraf")
 *     .range(-5L, ChronoUnit.MINUTES)
 *     .difference(new String[]{"_value", "_time"}, false)
 * </pre>
 *
 * @author Jakub Bednar (bednar@github) (17/07/2018 12:28)
 * @since 3.0.0
 */
public final class DifferenceFlux extends AbstractParametrizedFlux {

    public DifferenceFlux(@Nonnull final Flux source) {
        super(source);
    }

    @Nonnull
    @Override
    protected String operatorName() {
        return "difference";
    }

    /**
     * @param nonNegative indicates if the derivative is allowed to be negative
     * @return this
     */
    @Nonnull
    public DifferenceFlux withNonNegative(final boolean nonNegative) {

        this.withPropertyValue("nonNegative", nonNegative);

        return this;
    }

    /**
     * @param columns list of columns on which to compute the difference
     * @return this
     */
    @Nonnull
    public DifferenceFlux withColumns(@Nonnull final String[] columns) {

        Objects.requireNonNull(columns, "Columns are required");

        this.withPropertyValue("columns", columns);

        return this;
    }

    /**
     * @param columns list of columns on which to compute the difference
     * @return this
     */
    @Nonnull
    public DifferenceFlux withColumns(@Nonnull final Collection<String> columns) {

        Objects.requireNonNull(columns, "Columns are required");

        this.withPropertyValue("columns", columns);

        return this;
    }
}
