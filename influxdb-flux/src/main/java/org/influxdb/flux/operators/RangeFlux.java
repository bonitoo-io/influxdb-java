package org.influxdb.flux.operators;

import org.influxdb.flux.Flux;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

import static org.influxdb.flux.FluxChain.TimeInterval;

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
 *     from(db:"foo")
 *          |&gt; filter(fn: (r) =&gt; r["_measurement"] == "cpu" AND
 *                    r["_field"] == "usage_system")
 *          |&gt; range(start:-12h, stop: -15m)
 *
 *    from(db:"foo")
 *          |&gt; range(start: 2018-05-23T13:09:22.885021542Z)
 *          |&gt; derivative(unit:100ms)
 * </pre>
 *
 * @author Jakub Bednar (bednar@github) (26/06/2018 07:04)
 * @since 3.0.0
 */
public final class RangeFlux extends AbstractParametrizedFlux {

    private final Parameter<Instant> startInstant;
    private final Parameter<Instant> stopInstant;

    private final Parameter<TimeInterval> startInterval;
    private final Parameter<TimeInterval> stopInterval;

    public RangeFlux(@Nonnull final Flux flux) {
        super(flux);

        this.startInstant = null;
        this.stopInstant = null;

        this.startInterval = null;
        this.stopInterval = null;
    }

    public RangeFlux(@Nonnull final Flux source, @Nonnull final Instant start) {

        super(source);

        Objects.requireNonNull(start, "Start is required");

        this.startInstant = (m) -> start;
        this.stopInstant = new NotDefinedParameter<>();

        this.startInterval = new NotDefinedParameter<>();
        this.stopInterval = new NotDefinedParameter<>();
    }

    public RangeFlux(@Nonnull final Flux source, @Nonnull final Instant start, @Nonnull final Instant stop) {

        super(source);

        Objects.requireNonNull(start, "Start is required");
        Objects.requireNonNull(stop, "Stop is required");

        this.startInstant = (m) -> start;
        this.stopInstant = (m) -> stop;

        this.startInterval = new NotDefinedParameter<>();
        this.stopInterval = new NotDefinedParameter<>();
    }

    public RangeFlux(@Nonnull final Flux source, @Nonnull final Long start, @Nonnull final ChronoUnit unit) {

        super(source);

        Objects.requireNonNull(start, "Start is required");
        Objects.requireNonNull(unit, "Stop is required");

        this.startInstant = new NotDefinedParameter<>();
        this.stopInstant = new NotDefinedParameter<>();

        this.startInterval = (m) -> new TimeInterval(start, unit);
        this.stopInterval = new NotDefinedParameter<>();
    }

    public RangeFlux(@Nonnull final Flux source, @Nonnull final Long start,
                     @Nonnull final Long stop, @Nonnull final ChronoUnit unit) {

        super(source);

        Objects.requireNonNull(start, "Start is required");
        Objects.requireNonNull(stop, "Stop is required");
        Objects.requireNonNull(unit, "Stop is required");

        this.startInstant = new NotDefinedParameter<>();
        this.stopInstant = new NotDefinedParameter<>();

        this.startInterval = (m) -> new TimeInterval(start, unit);
        this.stopInterval = (m) -> new TimeInterval(stop, unit);
    }

    @Nonnull
    @Override
    String operatorName() {
        return "range";
    }

    @Nonnull
    @Override
    OperatorParameters getParameters() {

        return OperatorParameters
                .of("start", startInstant)
                .put("start", startInterval)
                .put("stop", stopInstant)
                .put("stop", stopInterval);
    }
}
