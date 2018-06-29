package org.influxdb.flux.operators;

import org.influxdb.flux.Flux;

import javax.annotation.Nonnull;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Objects;

/**
 * <a href="https://github.com/influxdata/platform/blob/master/query/docs/SPEC.md#shift">shift</a> -
 * Shift add a fixed duration to time columns.
 *
 * <h3>Options</h3>
 * <ul>
 * <li><b>shift</b> - The amount to add to each time value [duration]</li>
 * <li><b>columns</b> - The list of all columns that should be shifted.
 * Defaults `["_start", "_stop", "_time"]` [array of strings].</li>
 * </ul>
 *
 * <h3>Example</h3>
 * <pre>
 * Flux flux = Flux
 *     .from("telegraf")
 *     .shift(10L, ChronoUnit.HOURS);
 *
 * Flux flux = Flux
 *     .from("telegraf")
 *     .shift(10L, ChronoUnit.HOURS, new String[]{"_time", "custom"});
 * </pre>
 *
 * @author Jakub Bednar (bednar@github) (29/06/2018 10:27)
 * @since 3.0.0
 */
public final class ShiftFlux extends AbstractParametrizedFlux {

    public ShiftFlux(@Nonnull final Flux source) {
        super(source);
    }

    @Nonnull
    @Override
    String operatorName() {
        return "shift";
    }

    /**
     * @param amount The amount to add to each time value
     * @param unit   a {@code ChronoUnit} determining how to interpret the {@code amount} parameter
     * @return this
     */
    @Nonnull
    public ShiftFlux withShift(@Nonnull final Long amount, @Nonnull final ChronoUnit unit) {
        Objects.requireNonNull(amount, "Amount is required");
        Objects.requireNonNull(unit, "ChronoUnit is required");

        this.withPropertyValue("shift", amount, unit);

        return this;
    }

    /**
     * @param columns The list of all columns that should be shifted.
     * @return this
     */
    @Nonnull
    public ShiftFlux withColumns(@Nonnull final String[] columns) {

        Objects.requireNonNull(columns, "Columns are required");

        this.withPropertyValue("columns", columns);

        return this;
    }

    /**
     * @param columns The list of all columns that should be shifted.
     * @return this
     */
    @Nonnull
    public ShiftFlux withColumns(@Nonnull final Collection<String> columns) {

        Objects.requireNonNull(columns, "Columns are required");

        this.withPropertyValue("columns", columns);

        return this;
    }
}
