package org.influxdb.reactive.option;

import org.influxdb.impl.Preconditions;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import java.util.Objects;

/**
 * QueryOptions are used to configure query data from the InfluxDB.
 *
 * @author Jakub Bednar (bednar@github) (11/06/2018 14:02)
 * @since 3.0.0
 */
public final class QueryOptions {

    private static final int DEFAULT_CHUNK_SIZE = 10_000;

    private final int chunkSize;

    /**
     * Default configuration: chunk_size = 10_000.
     */
    public static final QueryOptions DEFAULTS = QueryOptions.builder().build();

    public QueryOptions(@Nonnull final Builder builder) {

        Objects.requireNonNull(builder, "QueryOptions.Builder is required");

        chunkSize = builder.chunkSize;
    }

    /**
     * @return the number of QueryResults to process in one chunk.
     * @see QueryOptions.Builder#chunkSize(int)
     * @since 3.0.0
     */
    public int getChunkSize() {
        return chunkSize;
    }

    /**
     * Creates a builder instance.
     *
     * @return a builder
     * @since 3.0.0
     */
    @Nonnull
    public static QueryOptions.Builder builder() {
        return new QueryOptions.Builder();
    }

    /**
     * A builder for {@code QueryOptions}.
     *
     * @since 3.0.0
     */
    @NotThreadSafe
    public static class Builder {

        private int chunkSize = DEFAULT_CHUNK_SIZE;

        /**
         * Set the number of QueryResults to process in one chunk.
         *
         * @param chunkSize the number of QueryResults to process in one chunk.
         * @return {@code this}
         * @since 3.0.0
         */
        @Nonnull
        public Builder chunkSize(final int chunkSize) {
            Preconditions.checkPositiveNumber(chunkSize, "chunkSize");
            this.chunkSize = chunkSize;
            return this;
        }

        /**
         * Build an instance of QueryOptions.
         *
         * @return {@code BatchOptionsReactive}
         */
        @Nonnull
        public QueryOptions build() {

            return new QueryOptions(this);
        }
    }

}
