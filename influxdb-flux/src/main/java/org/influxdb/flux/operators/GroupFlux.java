package org.influxdb.flux.operators;

import org.influxdb.flux.Flux;

import javax.annotation.Nonnull;

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
}
