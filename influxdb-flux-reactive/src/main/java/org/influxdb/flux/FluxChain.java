package org.influxdb.flux;

import org.influxdb.flux.options.query.AbstractOption;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * The utility for chaining Flux operators {@link org.influxdb.flux.operators}.
 *
 * @author Jakub Bednar (bednar@github) (22/06/2018 11:14)
 */
public final class FluxChain {

    private final StringBuilder builder = new StringBuilder();

    private Map<String, Object> parameters = new HashMap<>();
    private List<AbstractOption> options = new ArrayList<>();

    public FluxChain() {
    }

    /**
     * Add the Flux parameters.
     *
     * @param parameters parameters
     * @return the current {@link FluxChain}
     */
    @Nonnull
    public FluxChain addParameters(@Nonnull final Map<String, Object> parameters) {

        Objects.requireNonNull(parameters, "Parameters are required");

        this.parameters.putAll(parameters);

        return this;
    }

    /**
     * @return get bound parameters
     */
    @Nonnull
    public Map<String, Object> getParameters() {
        return parameters;
    }

    /**
     * Add the Flux query options.
     *
     * @param options Flux query options
     * @return the current {@link FluxChain}
     * @see org.influxdb.flux.options.query.TaskOption
     * @see org.influxdb.flux.options.query.NowOption
     */
    @Nonnull
    public FluxChain addOptions(@Nonnull final List<AbstractOption> options) {

        Objects.requireNonNull(options, "Options are required");

        this.options.addAll(options);

        return this;
    }

    /**
     * Appends the operator to the chain sequence.
     *
     * @param operator the incoming operator
     * @return the current {@link FluxChain}
     */
    @Nonnull
    public FluxChain append(@Nullable final CharSequence operator) {

        if (operator == null) {
            return this;
        }

        if (builder.length() != 0) {
            builder.append("\n");
            builder.append("\t|> ");
        }
        builder.append(operator);

        return this;
    }

    /**
     * Appends the {@code source} to the chain sequence.
     *
     * @param source the incoming {@link Flux} operator
     * @return the current {@link FluxChain}
     */
    @Nonnull
    public FluxChain append(@Nonnull final Flux source) {

        Objects.requireNonNull(source, "Flux source is required");

        source.appendActual(this);

        return this;
    }

    /**
     * @return operator chain
     */
    @Nonnull
    String print() {

        StringJoiner joiner = new StringJoiner("\n\n");

        options.forEach(option -> joiner.add(option.toString()));
        joiner.add(builder.toString());

        return joiner.toString();
    }

}
