package org.influxdb.flux.options;

import org.influxdb.flux.options.query.AbstractOption;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import javax.annotation.concurrent.ThreadSafe;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The settings for customize Flux query.
 *
 * @author Jakub Bednar (bednar@github) (16/07/2018 13:50)
 * @since 3.0.0
 */
@ThreadSafe
public final class FluxOptions {

    /**
     * Default FluxOptions settings.
     */
    public static final FluxOptions DEFAULTS = FluxOptions.builder().build();

    private final FluxCsvParserOptions parserOptions;
    private final List<AbstractOption> queryOptions = new ArrayList<>();

    private FluxOptions(@Nonnull final Builder builder) {

        Objects.requireNonNull(builder, "FluxOptions.Builder is required");

        this.parserOptions = builder.parserOptions;
        this.queryOptions.addAll(builder.variables);
    }

    /**
     * @return the CSV parser options
     * @see Builder#parserOptions(FluxCsvParserOptions)
     */
    @Nonnull
    public FluxCsvParserOptions getParserOptions() {
        return parserOptions;
    }

    /**
     * @return the Flux query options that define variables
     * @see Builder#addOption(AbstractOption)
     */
    @Nonnull
    public List<AbstractOption> getQueryOptions() {
        return queryOptions;
    }

    /**
     * Creates a builder instance.
     *
     * @return a builder
     * @since 3.0.0
     */
    @Nonnull
    public static FluxOptions.Builder builder() {
        return new FluxOptions.Builder();
    }

    /**
     * A builder for {@code FluxOptions}.
     *
     * @since 3.0.0
     */
    @NotThreadSafe
    public static class Builder {

        private FluxCsvParserOptions parserOptions = FluxCsvParserOptions.DEFAULTS;
        private List<AbstractOption> variables = new ArrayList<>();

        /**
         * Set the CSV parser options.
         *
         * @param parserOptions the CSV parser options. Defaults {@link FluxCsvParserOptions#DEFAULTS}.
         * @return {@code this}
         * @since 3.0.0
         */
        @Nonnull
        public FluxOptions.Builder parserOptions(@Nonnull final FluxCsvParserOptions parserOptions) {

            Objects.requireNonNull(parserOptions, "FluxCsvParserOptions is required");

            this.parserOptions = parserOptions;

            return this;
        }

        /**
         * Add option that define variables of Flux query.
         *
         * @param option option that define variables of Flux query
         * @return {@code this}
         * @see org.influxdb.flux.options.query.NowOption
         * @see org.influxdb.flux.options.query.TaskOption
         */
        @Nonnull
        public <O extends AbstractOption> FluxOptions.Builder addOption(@Nonnull final O option) {

            Objects.requireNonNull(parserOptions, "FluxCsvParserOptions is required");

            this.variables.add(option);

            return this;
        }


        /**
         * Build an instance of FluxOptions.
         *
         * @return {@link FluxOptions}
         */
        @Nonnull
        public FluxOptions build() {

            return new FluxOptions(this);
        }
    }
}
