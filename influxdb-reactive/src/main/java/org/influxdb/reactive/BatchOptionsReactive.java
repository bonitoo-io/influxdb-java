package org.influxdb.reactive;

import org.influxdb.BatchOptions;

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
    public static final BatchOptionsReactive DISABLED = BatchOptionsReactive.builder()
            .actions(1)
            .flushDuration(0)
            .build();

    private int actions;
    private int flushDuration;
    private int jitterDuration;
    private int bufferLimit;

    /**
     * @return actions the number of actions to collect
     * @see BatchOptionsReactive.Builder#actions(int)
     * @since 3.0.0
     */
    public int getActions() {
        return actions;
    }

    /**
     * @return flushDuration the time to wait at most (milliseconds)
     * @see BatchOptionsReactive.Builder#flushDuration(int) (int)
     * @since 3.0.0
     */
    public int getFlushDuration() {
        return flushDuration;
    }

    /**
     * @return batch flush interval jitter value (milliseconds)
     * @see BatchOptionsReactive.Builder#jitterDuration(int)
     * @since 3.0.0
     */
    public int getJitterDuration() {
        return jitterDuration;
    }

    /**
     * @return Maximum number of points stored in the retry buffer.
     * @see BatchOptionsReactive.Builder#bufferLimit(int)
     * @since 3.0.0
     */
    public int getBufferLimit() {
        return bufferLimit;
    }

    private BatchOptionsReactive(@Nonnull final Builder builder) {

        Objects.requireNonNull(builder, "BatchOptionsReactive.Builder is required");

        actions = builder.actions;
        flushDuration = builder.flushDuration;
        jitterDuration = builder.jitterDuration;
        bufferLimit = builder.bufferLimit;
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
     * A builder for {@code BatchOptionsReactive}.
     *
     * @since 3.0.0
     */
    @NotThreadSafe
    public static class Builder {

        private int actions = DEFAULT_BATCH_ACTIONS_LIMIT;
        private int flushDuration = DEFAULT_BATCH_INTERVAL_DURATION;
        private int jitterDuration = DEFAULT_JITTER_INTERVAL_DURATION;
        private int bufferLimit = DEFAULT_BUFFER_LIMIT;

        /**
         * Set the number of actions to collect.
         *
         * @param actions the number of actions to collect
         * @return {@code this}
         * @since 3.0.0
         */
        @Nonnull
        public Builder actions(final int actions) {
            this.actions = actions;
            return this;
        }

        /**
         * Set the time to wait at most (milliseconds).
         *
         * @param flushDuration the time to wait at most (milliseconds).
         * @return {@code this}
         * @since 3.0.0
         */
        @Nonnull
        public Builder flushDuration(final int flushDuration) {
            this.flushDuration = flushDuration;
            return this;
        }

        /**
         * Jitters the batch flush interval by a random amount. This is primarily to avoid
         * large write spikes for users running a large number of client instances.
         * ie, a jitter of 5s and flush duration 10s means flushes will happen every 10-15s.
         *
         * @param jitterDuration (milliseconds)
         * @return {@code this}
         * @since 3.0.0
         */
        @Nonnull
        public Builder jitterDuration(final int jitterDuration) {
            this.jitterDuration = jitterDuration;
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
            this.bufferLimit = bufferLimit;
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
