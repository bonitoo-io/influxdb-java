package org.influxdb.flux.operators;

import org.influxdb.flux.Flux;
import org.influxdb.impl.Preconditions;

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
 *     from(db: "telegraf") |&gt; range(start: -30m) |&gt; group(by: ["tag_a", "tag_b"])
 *
 *     from(db: "telegraf") |&gt; range(start: -30m) |&gt; group(by: ["tag_a", "tag_b"], keep:["tag_c"])
 *
 *     from(db: "telegraf") |&gt; range(start: -30m) |&gt; group(except: ["tag_a"], keep:["tag_b", "tag_c"])
 * </pre>
 *
 * @author Jakub Bednar (bednar@github) (25/06/2018 14:56)
 * @since 3.0.0
 */
public final class GroupFlux extends AbstractParametrizedFlux {

    private final Parameter<Collection<String>> groupBy;
    private final Parameter<Collection<String>> keep;
    private final Parameter<Collection<String>> except;

    public enum GroupType {
        GROUP_BY,
        EXCEPT
    }

    public GroupFlux(@Nonnull final Flux source,
                     @Nonnull final Collection<String> tags,
                     @Nonnull final Collection<String> keep,
                     @Nonnull final GroupType groupType) {

        super(source);

        Objects.requireNonNull(tags, "Tags are required");
        Objects.requireNonNull(keep, "Keep Tags are required");
        Objects.requireNonNull(groupType, "GroupType is required");

        this.keep = (m) -> keep;
        if (groupType.equals(GroupType.GROUP_BY)) {
            this.groupBy = (m) -> tags;
            this.except = new NotDefinedParameter<>();
        } else {
            this.groupBy = new NotDefinedParameter<>();
            this.except = (m) -> tags;
        }
    }

    public GroupFlux(@Nonnull final Flux source,
                     @Nonnull final Collection<String> tags,
                     @Nonnull final GroupType groupType) {

        super(source);

        Objects.requireNonNull(tags, "Tags are required");
        Objects.requireNonNull(groupType, "GroupType is required");

        this.keep = new NotDefinedParameter<>();
        if (groupType.equals(GroupType.GROUP_BY)) {
            this.groupBy = (m) -> tags;
            this.except = new NotDefinedParameter<>();
        } else {
            this.groupBy = new NotDefinedParameter<>();
            this.except = (m) -> tags;
        }
    }

    public GroupFlux(@Nonnull final Flux source,
                     @Nonnull final String tagParameterName,
                     @Nonnull final String keepParameterName,
                     @Nonnull final GroupType groupType) {

        super(source);

        Preconditions.checkNonEmptyString(tagParameterName, "Tags parameter name");
        Preconditions.checkNonEmptyString(keepParameterName, "Keep parameter name");
        Objects.requireNonNull(groupType, "GroupType is required");

        this.keep = new BoundParameter<>(keepParameterName);
        if (groupType.equals(GroupType.GROUP_BY)) {
            this.groupBy = new BoundParameter<>(tagParameterName);
            this.except = new NotDefinedParameter<>();
        } else {
            this.groupBy = new NotDefinedParameter<>();
            this.except = new BoundParameter<>(tagParameterName);
        }
    }

    public GroupFlux(@Nonnull final Flux source,
                     @Nonnull final String tagParameterName,
                     @Nonnull final GroupType groupType) {

        super(source);

        Preconditions.checkNonEmptyString(tagParameterName, "Tags parameter name");
        Objects.requireNonNull(groupType, "GroupType is required");

        this.keep = new NotDefinedParameter<>();
        if (groupType.equals(GroupType.GROUP_BY)) {
            this.groupBy = new BoundParameter<>(tagParameterName);
            this.except = new NotDefinedParameter<>();
        } else {
            this.groupBy = new NotDefinedParameter<>();
            this.except = new BoundParameter<>(tagParameterName);
        }
    }

    @Nonnull
    @Override
    String operatorName() {
        return "group";
    }

    @Nonnull
    @Override
    OperatorParameters getParameters() {

        return OperatorParameters
                .of("by", groupBy)
                .put("except", except)
                .put("keep", keep);
    }
}
