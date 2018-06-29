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
 * Flux flux = Flux
 *     .from("telegraf")
 *     .sort(new String[]{"region", "value"});
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

    public SortFlux(@Nonnull final Flux flux) {
        super(flux);
    }

    @Nonnull
    @Override
    String operatorName() {
        return "sort";
    }

    /**
     * @param desc use the descending sorting
     * @return this
     */
    @Nonnull
    public SortFlux withDesc(final boolean desc) {

        this.withPropertyValue("desc", desc);

        return this;
    }

    /**
     * @param columns columns used to sort
     * @return this
     */
    @Nonnull
    public SortFlux withCols(@Nonnull final String[] columns) {

        Objects.requireNonNull(columns, "Columns are required");

        this.withPropertyValue("cols", columns);

        return this;
    }

    /**
     * @param columns columns used to sort
     * @return this
     */
    @Nonnull
    public SortFlux withCols(@Nonnull final Collection<String> columns) {

        Objects.requireNonNull(columns, "Columns are required");

        this.withPropertyValue("cols", columns);

        return this;
    }
}
