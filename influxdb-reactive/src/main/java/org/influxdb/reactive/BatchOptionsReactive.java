package org.influxdb.reactive;

import io.reactivex.BackpressureOverflowStrategy;
import org.influxdb.BatchOptions;
import org.influxdb.impl.Preconditions;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import java.util.Objects;

import static org.influxdb.BatchOptions.DEFAULT_BATCH_ACTIONS_LIMIT;
import static org.influxdb.BatchOptions.DEFAULT_BATCH_INTERVAL_DURATION;
import static org.influxdb.BatchOptions.DEFAULT_BUFFER_LIMIT;
import static org.influxdb.BatchOptions.DEFAULT_JITTER_INTERVAL_DURATION;

/**
 * BatchOptions are used to configure batching of individual data point writes into InfluxDB.
 *
 * @author Jakub Bednar (bednar@github) (04/06/2018 14:09)
 * @since 3.0.0
 */
public final class BatchOptionsReactive {

    /**
     * Default configuration with values that are consistent with Telegraf.
     */
    public static final BatchOptionsReactive DEFAULTS = BatchOptionsReactive.builder().build();

    /**
     * Disabled batching.
     */
    public static final BatchOptionsReactive DISABLED = BatchOptionsReactive.disabled().build();

    private final int actions;
    private final int flushInterval;
    private final int jitterInterval;
    private final int retryInterval;
    private final int bufferLimit;
    private final BackpressureOverflowStrategy backpressureStrategy;

    /**
     * @return actions the number of actions to collect
     * @see BatchOptionsReactive.Builder#actions(int)
     * @since 3.0.0
     */
    public int getActions() {
        return actions;
    }

    /**
     * @return flushInterval the time to wait at most (milliseconds)
     * @see BatchOptionsReactive.Builder#flushInterval(int) (int)
     * @since 3.0.0
     */
    public int getFlushInterval() {
        return flushInterval;
    }

    /**
     * @return batch flush interval jitter value (milliseconds)
     * @see BatchOptionsReactive.Builder#jitterInterval(int)
     * @since 3.0.0
     */
    public int getJitterInterval() {
        return jitterInterval;
    }


    /**
     * @return the time to wait before retry unsuccessful write (milliseconds)
     * @see BatchOptionsReactive.Builder#retryInterval(int)
     * @since 3.0.0
     */
    public int getRetryInterval() {
        return retryInterval;
    }

    /**
     * @return Maximum number of points stored in the retry buffer.
     * @see BatchOptionsReactive.Builder#bufferLimit(int)
     * @since 3.0.0
     */
    public int getBufferLimit() {
        return bufferLimit;
    }

    /**
     * @return the strategy to deal with buffer overflow when using onBackpressureBuffer
     * @see BatchOptionsReactive.Builder#backpressureStrategy(BackpressureOverflowStrategy)
     * @since 3.0.0
     */
    @Nonnull
    public BackpressureOverflowStrategy getBackpressureStrategy() {
        return backpressureStrategy;
    }

    private BatchOptionsReactive(@Nonnull final Builder builder) {

        Objects.requireNonNull(builder, "BatchOptionsReactive.Builder is required");

        actions = builder.actions;
        flushInterval = builder.flushInterval;
        jitterInterval = builder.jitterInterval;
        retryInterval = builder.retryInterval;
        bufferLimit = builder.bufferLimit;
        backpressureStrategy = builder.backpressureStrategy;
    }

    /**
     * Creates a builder instance.
     *
     * @return a builder
     * @since 3.0.0
     */
    @Nonnull
    public static BatchOptionsReactive.Builder builder() {
        return new BatchOptionsReactive.Builder();
    }

    /**
     * Creates a builder instance with disabled batching.
     *
     * @return a builder
     * @since 3.0.0
     */
    @Nonnull
    public static BatchOptionsReactive.Builder disabled() {
        return BatchOptionsReactive.builder().actions(1);
    }

    /**
     * A builder for {@code BatchOptionsReactive}.
     *
     * @since 3.0.0
     */
    @NotThreadSafe
    public static class Builder {

        private int actions = DEFAULT_BATCH_ACTIONS_LIMIT;
        private int flushInterval = DEFAULT_BATCH_INTERVAL_DURATION;
        private int jitterInterval = DEFAULT_JITTER_INTERVAL_DURATION;
        private int retryInterval = DEFAULT_BATCH_INTERVAL_DURATION;
        private int bufferLimit = DEFAULT_BUFFER_LIMIT;
        private BackpressureOverflowStrategy backpressureStrategy = BackpressureOverflowStrategy.DROP_OLDEST;

        /**
         * Set the number of actions to collect.
         *
         * @param actions the number of actions to collect
         * @return {@code this}
         * @since 3.0.0
         */
        @Nonnull
        public Builder actions(final int actions) {
            Preconditions.checkPositiveNumber(actions, "actions");
            this.actions = actions;
            return this;
        }

        /**
         * Set the time to wait at most (milliseconds).
         *
         * @param flushInterval the time to wait at most (milliseconds).
         * @return {@code this}
         * @since 3.0.0
         */
        @Nonnull
        public Builder flushInterval(final int flushInterval) {
            Preconditions.checkPositiveNumber(flushInterval, "flushInterval");
            this.flushInterval = flushInterval;
            return this;
        }

        /**
         * Jitters the batch flush interval by a random amount. This is primarily to avoid
         * large write spikes for users running a large number of client instances.
         * ie, a jitter of 5s and flush duration 10s means flushes will happen every 10-15s.
         *
         * @param jitterInterval (milliseconds)
         * @return {@code this}
         * @since 3.0.0
         */
        @Nonnull
        public Builder jitterInterval(final int jitterInterval) {
            Preconditions.checkNotNegativeNumber(jitterInterval, "jitterInterval");
            this.jitterInterval = jitterInterval;
            return this;
        }

        /**
         * Set the the time to wait before retry unsuccessful write (milliseconds).
         *
         * @param retryInterval the time to wait before retry unsuccessful write
         * @return {@code this}
         * @since 3.0.0
         */
        @Nonnull
        public Builder retryInterval(final int retryInterval) {
            Preconditions.checkPositiveNumber(retryInterval, "retryInterval");
            this.retryInterval = retryInterval;
            return this;
        }

        /**
         * The client maintains a buffer for failed writes so that the writes will be retried later on. This may
         * help to overcome temporary network problems or InfluxDB load spikes.
         * When the buffer is full and new points are written, oldest entries in the buffer are lost.
         * <p>
         * To disable this feature set buffer limit to a value smaller than {@link BatchOptions#getActions}
         *
         * @param bufferLimit maximum number of points stored in the retry buffer
         * @return {@code this}
         * @since 3.0.0
         */
        @Nonnull
        public Builder bufferLimit(final int bufferLimit) {
            Preconditions.checkNotNegativeNumber(bufferLimit, "bufferLimit");
            this.bufferLimit = bufferLimit;
            return this;
        }

        /**
         * Set the strategy to deal with buffer overflow when using onBackpressureBuffer.
         *
         * @param backpressureStrategy the strategy to deal with buffer overflow when using onBackpressureBuffer.
         *                             Default {@link BackpressureOverflowStrategy#DROP_OLDEST};
         * @return {@code this}
         * @since 3.0.0
         */
        @Nonnull
        public Builder backpressureStrategy(@Nonnull final BackpressureOverflowStrategy backpressureStrategy) {
            Objects.requireNonNull(backpressureStrategy, "Backpressure Overflow Strategy is required");
            this.backpressureStrategy = backpressureStrategy;
            return this;
        }

        /**
         * Build an instance of BatchOptionsReactive.
         *
         * @return {@code BatchOptionsReactive}
         */
        @Nonnull
        public BatchOptionsReactive build() {


            return new BatchOptionsReactive(this);
        }
    }
}
