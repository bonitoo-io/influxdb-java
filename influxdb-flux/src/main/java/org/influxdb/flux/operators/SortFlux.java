package org.influxdb.flux.operators;

import org.influxdb.flux.Flux;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Objects;

/**
 * <a href="https://github.com/influxdata/platform/tree/master/query#sort">sort</a> - Sorts the results by the
 * specified columns Default sort is ascending.
 *
 * <h3>Options</h3>
 * <ul>
 * <li>
 * <b>cols</b> - List of columns used to sort; precedence from left to right.
 * Default is ["value"] [array of strings]
 * </li>
 * <li><b>desc</b> - Sort results descending. Default false [bool]</li>
 * </ul>
 *
 * <h3>Example</h3>
 * <pre>
 *     from(db:"telegraf")
 *          |&gt; filter(fn: (r) =&gt; r["_measurement"] == "system" AND r["_field"] == "uptime")
 *          |&gt; range(start:-12h)
 *          |&gt; sort(cols:["region", "host", "value"])
 *
 *     from(db:"telegraf")
 *          |&gt; filter(fn: (r) =&gt; r["_measurement"] == "system" AND r["_field"] == "uptime")
 *          |&gt; range(start:-12h)
 *          |&gt; sort(desc: true)
 * </pre>
 *
 * @author Jakub Bednar (bednar@github) (25/06/2018 13:20)
 * @since 3.0.0
 */
public final class SortFlux extends AbstractParametrizedFlux {

    private final Parameter<Boolean> desc;
    private final Parameter<Collection<String>> columns;

    public SortFlux(@Nonnull final Flux flux) {
        super(flux);

        this.desc = null;
        this.columns = null;
    }

    public SortFlux(@Nonnull final Flux source, final boolean desc) {
        super(source);

        this.desc = (m) -> desc;
        this.columns = new NotDefinedParameter<>();
    }

    public SortFlux(@Nonnull final Flux source, @Nonnull final Collection<String> columns) {
        super(source);

        Objects.requireNonNull(columns, "Columns are required");

        this.desc = new NotDefinedParameter<>();
        this.columns = (m) -> columns;
    }

    public SortFlux(@Nonnull final Flux source, @Nonnull final Collection<String> columns, final boolean desc) {
        super(source);

        Objects.requireNonNull(columns, "Columns are required");

        this.desc = (m) -> desc;
        this.columns = (m) -> columns;
    }

    @Nonnull
    @Override
    String operatorName() {
        return "sort";
    }

    @Nonnull
    @Override
    OperatorParameters getParameters() {

        return OperatorParameters.of("cols", columns).put("desc", desc);
    }
}
