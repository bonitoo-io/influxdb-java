package org.influxdb.flux.options;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import javax.annotation.concurrent.ThreadSafe;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * The settings for customize parsing the Flux CSV response.
 *
 * @author Jakub Bednar (bednar@github) (16/07/2018 12:42)
 * @since 3.0.0
 */
@ThreadSafe
public final class FluxCsvParserOptions {

    /**
     * Default FluxCsvParser settings.
     */
    public static final FluxCsvParserOptions DEFAULTS = FluxCsvParserOptions.builder().build();

    private final List<String> valueDestinations;

    private FluxCsvParserOptions(@Nonnull final Builder builder) {
        Objects.requireNonNull(builder, "FluxCsvParserOptions.Builder is required");

        valueDestinations = builder.valueDestinations;
    }

    /**
     * @return the column names of the record where result will be placed
     * @see Builder#valueDestinations(String...)
     * @since 3.0.0
     */
    @Nonnull
    public List<String> getValueDestinations() {
        return valueDestinations;
    }

    /**
     * Creates a builder instance.
     *
     * @return a builder
     * @since 3.0.0
     */
    @Nonnull
    public static FluxCsvParserOptions.Builder builder() {
        return new FluxCsvParserOptions.Builder();
    }

    /**
     * A builder for {@code FluxCsvParserOptions}.
     *
     * @since 3.0.0
     */
    @NotThreadSafe
    public static final class Builder {

        private List<String> valueDestinations = new ArrayList<>();

        private Builder() {
            valueDestinations.add("_value");
        }


        /**
         * Set the column names of the record where result will be placed.
         * <p>
         * Map function can produce multiple value columns:
         * <pre>
         * from(db:"foo")
         *     |&gt; filter(fn: (r) =&gt; r["_measurement"]=="cpu" AND
         *                 r["_field"] == "usage_system" AND
         *                 r["service"] == "app-server")
         *     |&gt; range(start:-12h)
         *     // Square the value and keep the original value
         *     |&gt; map(fn: (r) =&gt; ({value: r._value, value2:r._value * r._value}))
         * </pre>
         *
         * @param valueDestinations the column names of the record where result will be placed. Defaults "_value".
         * @return {@code this}
         * @since 3.0.0
         */
        @Nonnull
        public FluxCsvParserOptions.Builder valueDestinations(@Nonnull final String... valueDestinations) {
            Objects.requireNonNull(valueDestinations, "ValueDestinations is required");

            if (valueDestinations.length != 0) {
                this.valueDestinations = Arrays.asList(valueDestinations);
            }

            return this;
        }

        /**
         * Build an instance of FluxCsvParserOptions.
         *
         * @return {@link FluxCsvParserOptions}
         */
        @Nonnull
        public FluxCsvParserOptions build() {

            return new FluxCsvParserOptions(this);
        }
    }
}
