package org.influxdb.flux.options.query;

import org.influxdb.impl.Preconditions;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import java.util.Objects;

/**
 * The custom option is prepare for option specified by user.
 *
 * @author Jakub Bednar (bednar@github) (27/07/2018 07:47)
 * @since 3.0.0
 */
public final class CustomOption extends AbstractOption {

    private CustomOption(@Nonnull final Builder builder) {
        super(builder.optionName);

        Objects.requireNonNull(builder, "CustomOption.Builder is required");

        this.value = builder.optionValue;
    }

    /**
     * Creates a builder instance.
     *
     * @param optionName name of the option
     * @return a builder
     * @since 3.0.0
     */
    @Nonnull
    public static CustomOption.Builder builder(@Nonnull final String optionName) {

        Preconditions.checkNonEmptyString(optionName, "Option name");

        return new CustomOption.Builder(optionName);
    }

    /**
     * A builder for {@code CustomOption}.
     *
     * @since 3.0.0
     */
    @NotThreadSafe
    public static final class Builder {

        private final String optionName;
        private String optionValue;

        public Builder(@Nonnull final String optionName) {

            Preconditions.checkNonEmptyString(optionName, "Option name");

            this.optionName = optionName;
        }

        /**
         * Set the option value.
         *
         * @param optionValue option value
         * @return {@code this}
         * @since 3.0.0
         */
        @Nonnull
        public CustomOption.Builder value(@Nonnull final String optionValue) {

            Preconditions.checkNonEmptyString(optionValue, "Option value");

            this.optionValue = optionValue;

            return this;
        }

        /**
         * Build an instance of CustomOption.
         *
         * @return {@link CustomOption}
         */
        @Nonnull
        public CustomOption build() {

            if (optionValue == null || optionValue.isEmpty()) {
                throw new IllegalStateException("Option value has to be defined");
            }

            return new CustomOption(this);
        }
    }
}
