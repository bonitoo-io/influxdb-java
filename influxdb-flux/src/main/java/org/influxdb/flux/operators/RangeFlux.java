package org.influxdb.flux.operators;

import org.influxdb.flux.Flux;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * <a href="https://github.com/influxdata/platform/tree/master/query#range">range</a> - Filters the results by
 * time boundaries.
 *
 * <h3>Options</h3>
 * <ul>
 * <li><b>start</b> - Specifies the oldest time to be included in the results [duration or timestamp]</li>
 * <li>
 * <b>stop</b> - Specifies the exclusive newest time to be included in the results.
 * Defaults to "now". [duration or timestamp]
 * </li>
 * </ul>
 *
 * <h3>Example</h3>
 * <pre>
 * Flux flux = Flux
 *     .from("telegraf")
 *     .range(-12L, -1L, ChronoUnit.HOURS)
 * </pre>
 *
 * @author Jakub Bednar (bednar@github) (26/06/2018 07:04)
 * @since 3.0.0
 */
public final class RangeFlux extends AbstractParametrizedFlux {

    public RangeFlux(@Nonnull final Flux flux) {
        super(flux);
    }

    @Nonnull
    @Override
    protected String operatorName() {
        return "range";
    }

    /**
     * @param start Specifies the oldest time to be included in the results
     * @return this
     */
    @Nonnull
    public RangeFlux withStart(@Nonnull final Instant start) {

        Objects.requireNonNull(start, "Start is required");

        this.withPropertyValue("start", start);

        return this;
    }

    /**
     * @param start Specifies the oldest time to be included in the results
     * @param unit  a {@code ChronoUnit} determining how to interpret the {@code start} parameter
     * @return this
     */
    @Nonnull
    public RangeFlux withStart(@Nonnull final Long start, @Nonnull final ChronoUnit unit) {

        Objects.requireNonNull(start, "Start is required");
        Objects.requireNonNull(unit, "ChronoUnit is required");

        this.withPropertyValue("start", start, unit);

        return this;
    }

    /**
     * @param stop Specifies the exclusive newest time to be included in the results
     * @return this
     */
    @Nonnull
    public RangeFlux withStop(@Nonnull final Instant stop) {

        Objects.requireNonNull(stop, "Stop is required");

        this.withPropertyValue("stop", stop);

        return this;
    }

    /**
     * @param stop Specifies the exclusive newest time to be included in the results
     * @param unit  a {@code ChronoUnit} determining how to interpret the {@code start} parameter
     * @return this
     */
    @Nonnull
    public RangeFlux withStop(@Nonnull final Long stop, @Nonnull final ChronoUnit unit) {

        Objects.requireNonNull(stop, "Stop is required");
        Objects.requireNonNull(unit, "ChronoUnit is required");

        this.withPropertyValue("stop", stop, unit);

        return this;
    }
}
