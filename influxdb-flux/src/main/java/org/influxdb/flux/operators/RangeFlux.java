package org.influxdb.flux.operators;

import org.influxdb.flux.Flux;
import org.influxdb.flux.FluxChain;
import org.influxdb.impl.Preconditions;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
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

    private final FluxChain.FluxParameter<Instant> startInstant;
    private final FluxChain.FluxParameter<Instant> stopInstant;

    private final FluxChain.FluxParameter<TimeInterval> startInterval;
    private final FluxChain.FluxParameter<TimeInterval> stopInterval;

    public RangeFlux(@Nonnull final Flux source, @Nonnull final Instant start) {

        super(source);

        Objects.requireNonNull(start, "Start is required");

        this.startInstant = (m) -> start;
        this.stopInstant = new FluxChain.NotDefinedParameter<>();

        this.startInterval = new FluxChain.NotDefinedParameter<>();
        this.stopInterval = new FluxChain.NotDefinedParameter<>();
    }

    public RangeFlux(@Nonnull final Flux source, @Nonnull final Instant start, @Nonnull final Instant stop) {

        super(source);

        Objects.requireNonNull(start, "Start is required");
        Objects.requireNonNull(stop, "Stop is required");

        this.startInstant = (m) -> start;
        this.stopInstant = (m) -> stop;

        this.startInterval = new FluxChain.NotDefinedParameter<>();
        this.stopInterval = new FluxChain.NotDefinedParameter<>();
    }

    public RangeFlux(@Nonnull final Flux source, @Nonnull final String startParameterName) {

        super(source);

        Preconditions.checkNonEmptyString(startParameterName, "Start parameter name");

        this.startInstant = new FluxChain.BoundFluxParameter<>(startParameterName);
        this.stopInstant = new FluxChain.NotDefinedParameter<>();

        this.startInterval = new FluxChain.NotDefinedParameter<>();
        this.stopInterval = new FluxChain.NotDefinedParameter<>();
    }

    public RangeFlux(@Nonnull final Flux source,
                     @Nonnull final String startParameterName,
                     @Nonnull final String stopParameterName) {

        super(source);

        Preconditions.checkNonEmptyString(startParameterName, "Start parameter name");
        Preconditions.checkNonEmptyString(stopParameterName, "Stop parameter name");

        this.startInstant = new FluxChain.BoundFluxParameter<>(startParameterName);
        this.stopInstant = new FluxChain.BoundFluxParameter<>(stopParameterName);

        this.startInterval = new FluxChain.NotDefinedParameter<>();
        this.stopInterval = new FluxChain.NotDefinedParameter<>();
    }

    public RangeFlux(@Nonnull final Flux source, @Nonnull final Long start, @Nonnull final ChronoUnit unit) {

        super(source);

        Objects.requireNonNull(start, "Start is required");
        Objects.requireNonNull(unit, "Stop is required");

        this.startInstant = new FluxChain.NotDefinedParameter<>();
        this.stopInstant = new FluxChain.NotDefinedParameter<>();

        this.startInterval = (m) -> new TimeInterval(start, unit);
        this.stopInterval = new FluxChain.NotDefinedParameter<>();
    }

    public RangeFlux(@Nonnull final Flux source, @Nonnull final Long start,
                     @Nonnull final Long stop, @Nonnull final ChronoUnit unit) {

        super(source);

        Objects.requireNonNull(start, "Start is required");
        Objects.requireNonNull(stop, "Stop is required");
        Objects.requireNonNull(unit, "Stop is required");

        this.startInstant = new FluxChain.NotDefinedParameter<>();
        this.stopInstant = new FluxChain.NotDefinedParameter<>();

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
    List<NamedParameter> getParameters() {

        List<NamedParameter> parameters = new ArrayList<>();
        parameters.add(new NamedParameter("start", startInstant));
        parameters.add(new NamedParameter("start", startInterval));
        parameters.add(new NamedParameter("stop", stopInstant));
        parameters.add(new NamedParameter("stop", stopInterval));

        return parameters;
    }
}
