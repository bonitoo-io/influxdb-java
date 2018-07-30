package org.influxdb.flux.operators;

import org.influxdb.flux.Flux;
import org.influxdb.impl.Preconditions;

import javax.annotation.Nonnull;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Objects;

/**
 * <a href="https://github.com/influxdata/platform/blob/master/query/docs/SPEC.md#derivative">derivative</a> -
 * Computes the time based difference between subsequent non null records.
 *
 * <h3>Options</h3>
 * <ul>
 * <li><b>unit</b> - The time duration to use for the result [duration]</li>
 * <li><b>nonNegative</b> - Indicates if the derivative is allowed to be negative [boolean].</li>
 * <li><b>columns</b> - List of columns on which to compute the derivative [array of strings].</li>
 * <li><b>timeSrc</b> - The source column for the time values. Defaults to `_time` [string].</li>
 * </ul>
 *
 * <h3>Example</h3>
 * <pre>
 * Flux flux = Flux
 *     .from("telegraf")
 *     .derivative(1L, ChronoUnit.MINUTES);
 *
 * Flux flux = Flux
 *     .from("telegraf")
 *     .derivative()
 *         .withUnit(10L, ChronoUnit.DAYS)
 *         .withNonNegative(true)
 *         .withColumns(new String[]{"columnCompare_1", "columnCompare_2"})
 *         .withTimeSrc("_timeColumn");
 * </pre>
 *
 * @author Jakub Bednar (bednar@github) (03/07/2018 14:28)
 * @since 3.0.0
 */
public final class DerivativeFlux extends AbstractParametrizedFlux {

    public DerivativeFlux(@Nonnull final Flux source) {
        super(source);
    }

    @Nonnull
    @Override
    protected String operatorName() {
        return "derivative";
    }

    /**
     * @param duration the time duration to use for the result
     * @param unit     a {@code ChronoUnit} determining how to interpret the {@code duration} parameter
     * @return this
     */
    @Nonnull
    public DerivativeFlux withUnit(@Nonnull final Long duration, @Nonnull final ChronoUnit unit) {

        Objects.requireNonNull(duration, "Duration is required");
        Objects.requireNonNull(unit, "ChronoUnit is required");

        this.withPropertyValue("unit", duration, unit);

        return this;
    }

    /**
     * @param useStartTime Indicates if the derivative is allowed to be negative
     * @return this
     */
    @Nonnull
    public DerivativeFlux withNonNegative(final boolean useStartTime) {

        this.withPropertyValue("nonNegative", useStartTime);

        return this;
    }

    /**
     * @param columns List of columns on which to compute the derivative.
     * @return this
     */
    @Nonnull
    public DerivativeFlux withColumns(@Nonnull final String[] columns) {

        Objects.requireNonNull(columns, "Columns are required");

        this.withPropertyValue("columns", columns);

        return this;
    }

    /**
     * @param columns List of columns on which to compute the derivative.
     * @return this
     */
    @Nonnull
    public DerivativeFlux withColumns(@Nonnull final Collection<String> columns) {

        Objects.requireNonNull(columns, "Columns are required");

        this.withPropertyValue("columns", columns);

        return this;
    }

    /**
     * @param timeSrc The source column for the time values
     * @return this
     */
    @Nonnull
    public DerivativeFlux withTimeSrc(@Nonnull final String timeSrc) {

        Preconditions.checkNonEmptyString(timeSrc, "Time column");

        this.withPropertyValueEscaped("timeSrc", timeSrc);

        return this;
    }
}
