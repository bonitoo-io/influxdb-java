package org.influxdb.flux.operators;

import org.influxdb.flux.Flux;

import javax.annotation.Nonnull;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * <a href="https://github.com/influxdata/platform/blob/master/query/docs/SPEC.md#integral">integral</a> -
 * For each aggregate column, it outputs the area under the curve of non null records.
 * The curve is defined as function where the domain is the record times and the range is the record values.
 *
 * <h3>Options</h3>
 * <ul>
 * <li><b>unit</b> - Time duration to use when computing the integral [duration]</li>
 * </ul>
 *
 * <h3>Example</h3>
 * <pre>
 * Flux flux = Flux
 *     .from("telegraf")
 *     .integral(1L, ChronoUnit.MINUTES);
 * </pre>
 *
 * @author Jakub Bednar (bednar@github) (03/07/2018 12:33)
 * @since 3.0.0
 */
public final class IntegralFlux extends AbstractParametrizedFlux {

    public IntegralFlux(@Nonnull final Flux source) {
        super(source);
    }

    @Nonnull
    @Override
    protected String operatorName() {
        return "integral";
    }

    /**
     * @param duration Time duration to use when computing the integral
     * @param unit     a {@code ChronoUnit} determining how to interpret the {@code duration} parameter
     * @return this
     */
    @Nonnull
    public IntegralFlux withUnit(@Nonnull final Long duration, @Nonnull final ChronoUnit unit) {

        Objects.requireNonNull(duration, "Duration is required");
        Objects.requireNonNull(unit, "ChronoUnit is required");

        this.withPropertyValue("unit", duration, unit);

        return this;
    }
}
