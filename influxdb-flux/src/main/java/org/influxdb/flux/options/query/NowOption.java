package org.influxdb.flux.options.query;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * The now option is a function that returns a time value to be used as a proxy for the current system time.
 *
 * @author Jakub Bednar (bednar@github) (26/07/2018 12:14)
 * @since 3.0.0
 */
public final class NowOption extends AbstractOption {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm:ss.nnnnnnnnn'Z'")
            .withZone(ZoneId.of("UTC"));

    private NowOption(@Nonnull final Builder builder) {

        super("now");

        Objects.requireNonNull(builder, "NowOption.Builder is required");

        this.value = builder.function;
    }

    /**
     * Creates a builder instance.
     *
     * @return a builder
     * @since 3.0.0
     */
    @Nonnull
    public static NowOption.Builder builder() {
        return new NowOption.Builder();
    }

    /**
     * A builder for {@code NowOption}.
     *
     * @since 3.0.0
     */
    @NotThreadSafe
    public static final class Builder {

        private String function;

        /**
         * Set the static time.
         *
         * @param time static time
         * @return {@code this}
         * @since 3.0.0
         */
        @Nonnull
        public NowOption.Builder time(@Nonnull final Instant time) {

            Objects.requireNonNull(time, "Time is required");

            return function("() => " + DATE_FORMATTER.format(time));
        }

        /**
         * Set the function that return now.
         *
         * @param function the function that return now
         * @return {@code this}
         * @since 3.0.0
         */
        @Nonnull
        public NowOption.Builder function(@Nonnull final String function) {

            Objects.requireNonNull(function, "Function is required");

            this.function = function;

            return this;
        }

        /**
         * Build an instance of NowOption.
         *
         * @return {@link NowOption}
         */
        @Nonnull
        public NowOption build() {

            if (function == null || function.isEmpty()) {
                throw new IllegalStateException("function or time has to be defined");
            }

            return new NowOption(this);
        }
    }
}
