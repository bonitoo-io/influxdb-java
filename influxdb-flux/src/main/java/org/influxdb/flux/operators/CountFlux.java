package org.influxdb.flux.operators;

import org.influxdb.flux.Flux;
import org.influxdb.flux.FluxChain;

import javax.annotation.Nonnull;

/**
 * <a href="https://github.com/influxdata/platform/tree/master/query#count">count</a> - Counts the number of results.
 *
 * <h3>Options</h3>
 * <ul>
 * <li><b>useStartTime</b> - Use the start time as the timestamp of the resulting aggregate [boolean]
 * </ul>
 *
 * <h3>Example</h3>
 * <pre>
 *     from(db:"telegraf") |&gt; count()
 * </pre>
 *
 * @author Jakub Bednar (bednar@github) (22/06/2018 11:06)
 */
public final class CountFlux extends AbstractFluxWithUpstream {

    private Boolean useStartTime;

    public CountFlux(@Nonnull final Flux source) {
        super(source);
    }

    public CountFlux(@Nonnull final Flux source, final boolean useStartTime) {
        super(source);

        this.useStartTime = useStartTime;
    }

    @Override
    protected void appendAfterUpstream(@Nonnull final FluxChain fluxChain) {

        StringBuilder count = new StringBuilder();
        //
        // count(
        //
        count.append("count(");
        //
        //
        // useStartTime: false
        if (useStartTime != null) {
            count.append("useStartTime: ").append(useStartTime.toString().toLowerCase());
        }
        //
        // )
        //
        count.append(")");

        fluxChain.append(count);
    }
}
