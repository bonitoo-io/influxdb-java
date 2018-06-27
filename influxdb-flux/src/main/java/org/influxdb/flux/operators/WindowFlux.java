package org.influxdb.flux.operators;

import org.influxdb.flux.Flux;
import org.influxdb.flux.FluxChain.TimeInterval;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * <a href="https://github.com/influxdata/platform/tree/master/query#window">window</a> - Partitions the results by
 * a given time range.
 *
 * <h3>Options</h3>
 * <ul>
 * <li><b>every</b> - Duration of time between windows. Defaults to <i>period's</i> value. [duration]
 * <li><b>period</b> - Duration of the windowed partition. Defaults to <i>every's</i> value. [duration]
 * <li><b>start</b> - The time of the initial window partition. [time]
 * <li><b>round</b> - Rounds a window's bounds to the nearest duration. Defaults to <i>every's</i> value. [duration]
 * <li><b>column</b> - Name of the time column to use. Defaults to <i>_time</i>. [string]
 * <li><b>startCol</b> - Name of the column containing the window start time. Defaults to <i>_start</i>. [string]
 * <li><b>stopCol</b> - Name of the column containing the window stop time. Defaults to <i>_stop</i>. [string]
 * </ul>
 *
 * <h3>Example</h3>
 * <pre>
 *     from(db: "telegraf") |&gt; range(start: -12h) |&gt; window(every: 10m) |&gt; max()
 *
 *     from(db: "telegraf") |&gt; range(start: -12h) |&gt; window(every: 1m, period: 1h, start: -4h, round: 1s)
 *
 * </pre>
 *
 * @author Jakub Bednar (bednar@github) (27/06/2018 12:01)
 * @since 3.0.0
 */
public final class WindowFlux extends AbstractParametrizedFlux {

    private final Parameter<TimeInterval> every;
    private final Parameter<TimeInterval> period;

    private final Parameter<Instant> startInstant;
    private final Parameter<TimeInterval> startInterval;

    private final Parameter<TimeInterval> round;

    private final Parameter<String> timeColumn;
    private final Parameter<String> startCol;
    private final Parameter<String> stopCol;

    public WindowFlux(@Nonnull final Flux flux) {
        super(flux);

        this.every = null;
        this.period = null;
        this.startInstant = null;
        this.startInterval = null;
        this.round = null;
        this.timeColumn = null;
        this.startCol = null;
        this.stopCol = null;
    }

    public WindowFlux(@Nonnull final Flux source,
                      @Nonnull final Long every,
                      @Nonnull final ChronoUnit everyUnit,
                      @Nullable final Long period,
                      @Nullable final ChronoUnit periodUnit,
                      @Nullable final Long start,
                      @Nullable final ChronoUnit startUnit,
                      @Nullable final Long round,
                      @Nullable final ChronoUnit roundUnit,
                      @Nullable final String timeColumn,
                      @Nullable final String startCol,
                      @Nullable final String stopCol) {

        super(source);

        this.every = (m) -> new TimeInterval(every, everyUnit);
        this.period = (m) -> new TimeInterval(period, periodUnit);
        this.startInterval = (m) -> new TimeInterval(start, startUnit);
        this.startInstant = new NotDefinedParameter<>();
        this.round = (m) -> new TimeInterval(round, roundUnit);

        this.timeColumn = new StringParameter(timeColumn);
        this.startCol = new StringParameter(startCol);
        this.stopCol = new StringParameter(stopCol);
    }

    public WindowFlux(@Nonnull final Flux source,
                      @Nonnull final Long every,
                      @Nonnull final ChronoUnit everyUnit,
                      @Nullable final Long period,
                      @Nullable final ChronoUnit periodUnit,
                      @Nullable final Instant start,
                      @Nullable final Long round,
                      @Nullable final ChronoUnit roundUnit,
                      @Nullable final String timeColumn,
                      @Nullable final String startCol,
                      @Nullable final String stopCol) {

        super(source);

        this.every = (m) -> new TimeInterval(every, everyUnit);
        this.period = (m) -> new TimeInterval(period, periodUnit);
        this.startInterval = new NotDefinedParameter<>();
        this.startInstant = (m) -> start;
        this.round = (m) -> new TimeInterval(round, roundUnit);

        this.timeColumn = new StringParameter(timeColumn);
        this.startCol = new StringParameter(startCol);
        this.stopCol = new StringParameter(stopCol);
    }

    @Nonnull
    @Override
    String operatorName() {
        return "window";
    }

    @Nonnull
    @Override
    OperatorParameters getParameters() {

        return OperatorParameters
                .of("every", every)
                .put("period", period)
                .put("start", startInterval)
                .put("start", startInstant)
                .put("round", round)
                .put("column", timeColumn)
                .put("startCol", startCol)
                .put("stopCol", stopCol);
    }
}
