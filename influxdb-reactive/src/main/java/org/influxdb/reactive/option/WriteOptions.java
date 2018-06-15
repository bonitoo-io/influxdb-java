package org.influxdb.reactive.option;

import org.influxdb.InfluxDB;
import org.influxdb.impl.Preconditions;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static org.influxdb.InfluxDBOptions.DEFAULT_CONSISTENCY_LEVEL;
import static org.influxdb.InfluxDBOptions.DEFAULT_PRECISION;
import static org.influxdb.InfluxDBOptions.DEFAULT_RETENTION_POLICY;

/**
 * WriteOptions are used to configure writes to the InfluxDB.
 *
 * @author Jakub Bednar (bednar@github) (14/06/2018 15:44)
 * @since 3.0.0
 */
public final class WriteOptions {

    private final String database;
    private final String retentionPolicy;
    private final InfluxDB.ConsistencyLevel consistencyLevel;
    private final TimeUnit precision;

    private WriteOptions(@Nonnull final Builder builder) {

        Objects.requireNonNull(builder, "WriteOptions.Builder is required");

        database = builder.database;
        retentionPolicy = builder.retentionPolicy;
        consistencyLevel = builder.consistencyLevel;
        precision = builder.precision;
    }

    /**
     * @return the name of the database to write
     */
    @Nonnull
    public String getDatabase() {
        return database;
    }

    /**
     * @return the retentionPolicy to use
     */
    @Nonnull
    public String getRetentionPolicy() {
        return retentionPolicy;
    }

    /**
     * @return the ConsistencyLevel to use
     */
    @Nonnull
    public InfluxDB.ConsistencyLevel getConsistencyLevel() {
        return consistencyLevel;
    }

    /**
     * @return the time precision to use
     */
    @Nonnull
    public TimeUnit getPrecision() {
        return precision;
    }

    /**
     * Creates a builder instance.
     *
     * @return a builder
     * @since 3.0.0
     */
    @Nonnull
    public static WriteOptions.Builder builder() {
        return new WriteOptions.Builder();
    }

    /**
     * A builder for {@code WriteOptions}.
     *
     * @since 3.0.0
     */
    @NotThreadSafe
    public static class Builder {

        private String database;
        private String retentionPolicy = DEFAULT_RETENTION_POLICY;
        private InfluxDB.ConsistencyLevel consistencyLevel = DEFAULT_CONSISTENCY_LEVEL;
        private TimeUnit precision = DEFAULT_PRECISION;

        /**
         * Set the name of the database to write.
         *
         * @param database the name of the database to write
         * @return {@code this}
         * @since 3.0.0
         */
        @Nonnull
        public Builder database(@Nonnull final String database) {

            Preconditions.checkNonEmptyString(database, "database");

            this.database = database;
            return this;
        }

        /**
         * Set the retentionPolicy to use.
         *
         * @param retentionPolicy the retentionPolicy to use
         * @return {@code this}
         * @since 3.0.0
         */
        @Nonnull
        public Builder retentionPolicy(@Nonnull final String retentionPolicy) {

            Preconditions.checkNonEmptyString(retentionPolicy, "retentionPolicy");

            this.retentionPolicy = retentionPolicy;
            return this;
        }

        /**
         * Set the ConsistencyLevel to use.
         *
         * @param consistencyLevel the ConsistencyLevel to use
         * @return {@code this}
         * @since 3.0.0
         */
        @Nonnull
        public Builder consistencyLevel(@Nonnull final InfluxDB.ConsistencyLevel consistencyLevel) {

            Objects.requireNonNull(consistencyLevel, "InfluxDB.ConsistencyLevel is required");

            this.consistencyLevel = consistencyLevel;
            return this;
        }

        /**
         * Set the time precision to use.
         *
         * @param precision the time precision to use
         * @return {@code this}
         * @since 3.0.0
         */
        @Nonnull
        public Builder precision(@Nonnull final TimeUnit precision) {

            Objects.requireNonNull(precision, "TimeUnit precision is required");

            this.precision = precision;
            return this;
        }

        /**
         * Build an instance of WriteOptions.
         *
         * @return {@code WriteOptions}
         */
        @Nonnull
        public WriteOptions build() {

            Preconditions.checkNonEmptyString(database, "database");

            return new WriteOptions(this);
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof WriteOptions)) {
            return false;
        }
        WriteOptions that = (WriteOptions) o;
        return Objects.equals(database, that.database)
                && Objects.equals(retentionPolicy, that.retentionPolicy)
                && consistencyLevel == that.consistencyLevel
                && precision == that.precision;
    }

    @Override
    public int hashCode() {
        return Objects.hash(database, retentionPolicy, consistencyLevel, precision);
    }

    @Override
    public String toString() {
        return "org.influxdb.reactive.option.WriteOptions{"
                + "database='" + database + '\''
                + ", retentionPolicy='" + retentionPolicy + '\''
                + ", consistencyLevel=" + consistencyLevel
                + ", precision=" + precision
                + '}';
    }
}

