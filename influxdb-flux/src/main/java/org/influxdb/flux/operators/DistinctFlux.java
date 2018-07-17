package org.influxdb.flux.operators;

import org.influxdb.flux.Flux;
import org.influxdb.impl.Preconditions;

import javax.annotation.Nonnull;

/**
 * <a href="https://github.com/influxdata/platform/blob/master/query/docs/SPEC.md#distinct">distinct</a> - Distinct
 * produces the unique values for a given column.
 *
 * <h3>Options</h3>
 * <ul>
 * <li>
 * <b>column</b> - The column on which to track unique values [string]
 * </li>
 * </ul>
 *
 * <h3>Example</h3>
 * <pre>
 * </pre>
 *
 * @author Jakub Bednar (bednar@github) (17/07/2018 12:08)
 * @since 3.0.0
 */
public final class DistinctFlux extends AbstractParametrizedFlux {

    public DistinctFlux(@Nonnull final Flux source) {
        super(source);
    }

    @Nonnull
    @Override
    protected String operatorName() {
        return "distinct";
    }

    /**
     * @param column The column on which to track unique values.
     * @return this
     */
    @Nonnull
    public DistinctFlux withColumn(@Nonnull final String column) {

        Preconditions.checkNonEmptyString(column, "Column");

        this.withPropertyValueEscaped("column", column);

        return this;
    }
}
