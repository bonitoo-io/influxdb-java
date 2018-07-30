package org.influxdb.flux.options.query;

import org.influxdb.flux.operators.properties.TimeInterval;
import org.influxdb.impl.Preconditions;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * The task option is used by a scheduler to schedule the execution of a Flux query.
 *
 * @author Jakub Bednar (bednar@github) (26/07/2018 12:14)
 * @since 3.0.0
 */
public final class TaskOption extends AbstractOption {

    private TaskOption(@Nonnull final Builder builder) {

        super("task");

        Objects.requireNonNull(builder, "TaskOption.Builder is required");

        StringJoiner joiner = new StringJoiner(",\n", "{\n", "\n}")
                .add(keyValue("name", builder.name));

        // every
        if (builder.every != null) {
            joiner.add(keyValue("every", new TimeInterval(builder.every, builder.everyUnit)));
        }
        // delay
        if (builder.delay != null) {
            joiner.add(keyValue("delay", new TimeInterval(builder.delay, builder.delayUnit)));
        }
        // cron
        if (builder.cron != null) {
            joiner.add(keyValue("cron", builder.cron));
        }
        // retry
        if (builder.retry != null) {
            joiner.add(keyValue("retry", builder.retry));
        }

        value = joiner.toString();
    }

    @Nonnull
    private CharSequence keyValue(@Nonnull final String key, @Nonnull final Object value) {

        Preconditions.checkNonEmptyString(key, "Key");
        Objects.requireNonNull(value, "Value");

        Object formatted = value;
        if (value instanceof String) {
            formatted = "\"" + value + "\"";
        }

        return String.format("\t%s: %s", key, formatted);
    }

    /**
     * Creates a builder instance.
     *
     * @return a builder
     * @since 3.0.0
     */
    @Nonnull
    public static TaskOption.Builder builder(@Nonnull final String name) {
        Preconditions.checkNonEmptyString(name, "Task name");

        return new TaskOption.Builder(name);
    }

    /**
     * A builder for {@code TaskOption}.
     *
     * @since 3.0.0
     */
    @NotThreadSafe
    public static final class Builder {

        private String name;

        private Long every;
        private ChronoUnit everyUnit;

        private Long delay;
        private ChronoUnit delayUnit;

        private String cron;

        private Integer retry;

        private Builder(@Nonnull final String name) {

            Preconditions.checkNonEmptyString(name, "Task name");

            this.name = name;
        }

        /**
         * Set interval that task should be run.
         *
         * @param every     task should be run at this interval
         * @param everyUnit a {@code ChronoUnit} determining how to interpret the {@code every}
         * @return {@code this}
         * @since 3.0.0
         */
        @Nonnull
        public TaskOption.Builder every(@Nonnull final Long every,
                                        @Nonnull final ChronoUnit everyUnit) {

            Objects.requireNonNull(every, "Every is required");
            Objects.requireNonNull(everyUnit, "Every ChronoUnit is required");

            this.every = every;
            this.everyUnit = everyUnit;

            return this;
        }

        /**
         * Set duration that delay scheduling this task.
         *
         * @param delay     delay scheduling this task by this duration
         * @param delayUnit a {@code ChronoUnit} determining how to interpret the {@code delay}
         * @return {@code this}
         * @since 3.0.0
         */
        @Nonnull
        public TaskOption.Builder delay(@Nonnull final Long delay,
                                        @Nonnull final ChronoUnit delayUnit) {

            Objects.requireNonNull(delay, "Delay is required");
            Objects.requireNonNull(delayUnit, "Delay ChronoUnit is required");

            this.delay = delay;
            this.delayUnit = delayUnit;

            return this;
        }

        /**
         * Set cron cron to schedule task.
         * <p>
         * Every and cron are mutually exclusive.
         *
         * @param expression cron expression to schedule task
         * @return {@code this}
         * @since 3.0.0
         */
        @Nonnull
        public TaskOption.Builder cron(@Nonnull final String expression) {

            Preconditions.checkNonEmptyString(expression, "Cron cron");

            this.cron = expression;

            return this;
        }

        /**
         * Set number of times to retry a failed query.
         *
         * @param retry number of times to retry a failed query
         * @return {@code this}
         * @since 3.0.0
         */
        @Nonnull
        public TaskOption.Builder retry(@Nonnull final Integer retry) {

            Preconditions.checkNotNegativeNumber(retry, "Number");

            this.retry = retry;

            return this;
        }

        /**
         * Build an instance of TaskOption.
         *
         * @return {@link TaskOption}
         */
        @Nonnull
        public TaskOption build() {

            return new TaskOption(this);
        }
    }
}
