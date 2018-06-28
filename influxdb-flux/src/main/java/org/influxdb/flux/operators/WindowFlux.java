package org.influxdb.flux.operators;

import org.influxdb.flux.Flux;

import javax.annotation.Nonnull;

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

    public WindowFlux(@Nonnull final Flux flux) {
        super(flux);
    }


    @Nonnull
    @Override
    String operatorName() {
        return "window";
    }
}
