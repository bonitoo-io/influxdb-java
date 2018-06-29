package org.influxdb.flux.operators;

import org.influxdb.flux.Flux;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Objects;

/**
 * <a href="https://github.com/influxdata/platform/tree/master/query#group">group</a> - Groups results by
 * a user-specified set of tags.
 *
 * <h3>Options</h3>
 * <ul>
 * <li><b>by</b> - Group by these specific tag names. Cannot be used with <i>except</i> option. [array of strings]
 * <li><b>keep</b> - Keep specific tag keys that were not in <i>by</i> in the results. [array of strings]
 * <li><b>except</b> - Group by all but these tag keys. Cannot be used with <i>by</i> option. [array of strings]
 * </ul>
 *
 * <h3>Example</h3>
 * <pre>
 *     Flux.from("telegraf")
 *                 .range(-30L, ChronoUnit.MINUTES)
 *                 .groupBy(new String[]{"tag_a", "tag_b"});
 *
 *     Flux.from("telegraf")
 *                 .range(-30L, ChronoUnit.MINUTES)
 *                 .groupBy(new String[]{"tag_a", "tag_b"}, new String[]{"tag_c"});
 *
 *     Flux.from("telegraf")
 *                 .range(-30L, ChronoUnit.MINUTES)
 *                 .groupExcept(new String[]{"tag_a"}, new String[]{"tag_b", "tag_c"});
 * </pre>
 *
 * @author Jakub Bednar (bednar@github) (25/06/2018 14:56)
 * @since 3.0.0
 */
public final class GroupFlux extends AbstractParametrizedFlux {

    public GroupFlux(@Nonnull final Flux source) {

        super(source);
    }

    @Nonnull
    @Override
    String operatorName() {
        return "group";
    }

    /**
     * @param groupBy Group by these specific tag names.
     * @return this
     */
    @Nonnull
    public GroupFlux withBy(@Nonnull final String[] groupBy) {

        Objects.requireNonNull(groupBy, "GroupBy Columns are required");

        this.addPropertyValue("by", groupBy);

        return this;
    }

    /**
     * @param groupBy Group by these specific tag names.
     * @return this
     */
    @Nonnull
    public GroupFlux withBy(@Nonnull final Collection<String> groupBy) {

        Objects.requireNonNull(groupBy, "GroupBy Columns are required");

        this.addPropertyValue("by", groupBy);

        return this;
    }

    /**
     * @param keep Keep specific tag keys that were not in {@code groupBy} in the results.
     * @return this
     */
    @Nonnull
    public GroupFlux withKeep(@Nonnull final String[] keep) {

        Objects.requireNonNull(keep, "Keep Columns are required");

        this.addPropertyValue("keep", keep);

        return this;
    }

    /**
     * @param keep Keep specific tag keys that were not in {@code groupBy} in the results.
     * @return this
     */
    @Nonnull
    public GroupFlux withKeep(@Nonnull final Collection<String> keep) {

        Objects.requireNonNull(keep, "Keep Columns are required");

        this.addPropertyValue("keep", keep);

        return this;
    }

    /**
     * @param except Group by all but these tag keys Cannot be used.
     * @return this
     */
    @Nonnull
    public GroupFlux withExcept(@Nonnull final String[] except) {

        Objects.requireNonNull(except, "GroupBy Except Columns are required");

        this.addPropertyValue("except", except);

        return this;
    }

    /**
     * @param except Group by all but these tag keys Cannot be used.
     * @return this
     */
    @Nonnull
    public GroupFlux withExcept(@Nonnull final Collection<String> except) {

        Objects.requireNonNull(except, "GroupBy Except Columns are required");

        this.addPropertyValue("except", except);

        return this;
    }
}
