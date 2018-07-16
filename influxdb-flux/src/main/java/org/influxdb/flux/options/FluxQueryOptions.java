package org.influxdb.flux.options;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import javax.annotation.concurrent.ThreadSafe;
import java.util.Objects;

/**
 * The settings for customize Flux query.
 *
 * @author Jakub Bednar (bednar@github) (16/07/2018 13:50)
 * @since 3.0.0
 */
@ThreadSafe
public final class FluxQueryOptions {

    /**
     * Default FluxQueryOptions settings.
     */
    public static final FluxQueryOptions DEFAULTS = FluxQueryOptions.builder().build();

    private final FluxCsvParserOptions parserOptions;

    private FluxQueryOptions(@Nonnull final Builder builder) {

        Objects.requireNonNull(builder, "FluxQueryOptions.Builder is required");

        this.parserOptions = builder.parserOptions;
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
     * Creates a builder instance.
     *
     * @return a builder
     * @since 3.0.0
     */
    @Nonnull
    public static FluxQueryOptions.Builder builder() {
        return new FluxQueryOptions.Builder();
    }

    /**
     * A builder for {@code FluxQueryOptions}.
     *
     * @since 3.0.0
     */
    @NotThreadSafe
    public static class Builder {

        private FluxCsvParserOptions parserOptions = FluxCsvParserOptions.DEFAULTS;

        /**
         * Set the CSV parser options.
         *
         * @param parserOptions the CSV parser options. Defaults {@link FluxCsvParserOptions#DEFAULTS}.
         * @return {@code this}
         * @since 3.0.0
         */
        @Nonnull
        public FluxQueryOptions.Builder parserOptions(@Nonnull final FluxCsvParserOptions parserOptions) {

            Objects.requireNonNull(parserOptions, "FluxCsvParserOptions is required");

            this.parserOptions = parserOptions;

            return this;
        }

        /**
         * Build an instance of FluxQueryOptions.
         *
         * @return {@link FluxQueryOptions}
         */
        @Nonnull
        public FluxQueryOptions build() {

            return new FluxQueryOptions(this);
        }
    }
}
