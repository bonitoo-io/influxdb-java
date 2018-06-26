package org.influxdb;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import org.influxdb.impl.Preconditions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


/**
 * Various settings to control the behavior of a {@code InfluxDB}.
 *
 * @author Jakub Bednar (bednar@github) (01/06/2018 07:53)
 * @see InfluxDB
 * @see InfluxDBFactory
 * @since 3.0.0
 */
public final class InfluxDBOptions {

    public static final String DEFAULT_RETENTION_POLICY = "autogen";
    public static final InfluxDB.ConsistencyLevel DEFAULT_CONSISTENCY_LEVEL = InfluxDB.ConsistencyLevel.ONE;
    public static final TimeUnit DEFAULT_PRECISION = TimeUnit.NANOSECONDS;

    private String url;

    private String username;
    private String password;

    private String database;
    private String retentionPolicy;
    private InfluxDB.ConsistencyLevel consistencyLevel;

    private TimeUnit precision;
    private MediaType mediaType;

    private OkHttpClient.Builder okHttpClient;
    private List<InfluxDBEventListener> listeners;

    private InfluxDBOptions(@Nonnull final Builder builder) {

        Objects.requireNonNull(builder, "InfluxDBOptions.Builder is required");

        url = builder.url;

        username = builder.username;
        password = builder.password;


        database = builder.database;
        retentionPolicy = builder.retentionPolicy;
        consistencyLevel = builder.consistencyLevel;

        precision = builder.precision;
        mediaType = builder.mediaType;

        okHttpClient = builder.okHttpClient;
        listeners =  Collections.unmodifiableList(builder.listeners);
    }

    /**
     * The url to connect to InfluxDB.
     *
     * @return url
     * @since 3.0.0
     */
    @Nonnull
    public String getUrl() {
        return url;
    }

    /**
     * The username which is used to authorize against the InfluxDB instance.
     *
     * @return username
     * @since 3.0.0
     */
    @Nullable
    public String getUsername() {
        return username;
    }

    /**
     * The password for the username which is used to authorize against the InfluxDB instance.
     *
     * @return password
     * @since 3.0.0
     */
    @Nullable
    public String getPassword() {
        return password;
    }

    /**
     * The database which is used for writing points.
     *
     * @return database
     * @since 3.0.0
     */
    @Nullable
    public String getDatabase() {
        return database;
    }

    /**
     * The retention policy which is used for writing points.
     *
     * @return retention policy
     * @since 3.0.0
     */
    @Nonnull
    public String getRetentionPolicy() {
        return retentionPolicy;
    }

    /**
     * The consistency level which is used for writing points.
     *
     * @return retention policy
     * @since 3.0.0
     */
    @Nonnull
    public InfluxDB.ConsistencyLevel getConsistencyLevel() {
        return consistencyLevel;
    }

    /**
     * The default TimeUnit of the interval.
     *
     * @return time unit
     * @since 3.0.0
     */
    @Nonnull
    public TimeUnit getPrecision() {
        return precision;
    }

    /**
     * The encoding of the point's data.
     *
     * @return time unit
     * @since 3.0.0
     */
    @Nonnull
    public MediaType getMediaType() {
        return mediaType;
    }

    /**
     * The HTTP client to use for communication to InfluxDB.
     *
     * @return okHttpClient
     * @since 3.0.0
     */
    @Nonnull
    public OkHttpClient.Builder getOkHttpClient() {
        return okHttpClient;
    }

    /**
     * Returns list of listeners registered by this client.
     * @since 3.0.0
     * @return unmodifiable list of listeners
     */
    @Nonnull
    public List<InfluxDBEventListener> getListeners() {
        return listeners;
    }

    /**
     * Creates a builder instance.
     *
     * @return a builder
     * @since 3.0.0
     */
    @Nonnull
    public static Builder builder() {
        return new Builder();
    }
    /**
     * A builder for {@code InfluxDBOptions}.
     *
     * @since 3.0.0
     */
    @NotThreadSafe
    public static class Builder {

        private String url;

        private String username;
        private String password;

        private String database;

        private String retentionPolicy = DEFAULT_RETENTION_POLICY;
        private InfluxDB.ConsistencyLevel consistencyLevel = DEFAULT_CONSISTENCY_LEVEL;
        private TimeUnit precision = DEFAULT_PRECISION;

        private MediaType mediaType = MediaType.parse("text/plain; charset=utf-8");

        private OkHttpClient.Builder okHttpClient = new OkHttpClient.Builder();
        private List<InfluxDBEventListener> listeners = new ArrayList<>();

        /**
         * Set the url to connect to InfluxDB.
         *
         * @param url the url to connect to InfluxDB. It must be defined.
         * @return {@code this}
         * @since 3.0.0
         */
        @Nonnull
        public Builder url(@Nonnull final String url) {
            Preconditions.checkNonEmptyString(url, "url");
            this.url = url;
            return this;
        }

        /**
         * Set the username which is used to authorize against the InfluxDB instance.
         *
         * @param username the username which is used to authorize against the InfluxDB instance. It may be null.
         * @return {@code this}
         * @since 3.0.0
         */
        @Nonnull
        public Builder username(@Nullable final String username) {
            this.username = username;
            return this;
        }

        /**
         * Set the password for the username which is used to authorize against the InfluxDB instance.
         *
         * @param password the password for the username which is used to authorize against the InfluxDB
         *                 instance. It may be null.
         * @return {@code this}
         * @since 3.0.0
         */
        @Nonnull
        public Builder password(@Nullable final String password) {
            this.password = password;
            return this;
        }

        /**
         * Set the database which is used for writing points.
         *
         * @param database the database to set.
         * @return {@code this}
         * @since 3.0.0
         */
        @Nonnull
        public Builder database(@Nullable final String database) {
            this.database = database;
            return this;
        }

        /**
         * Set the retention policy which is used for writing points.
         *
         * @param retentionPolicy the retention policy to set. It may be null.
         *                        If null than use default policy "autogen".
         * @return {@code this}
         * @since 3.0.0
         */
        @Nonnull
        public Builder retentionPolicy(@Nullable final String retentionPolicy) {

            if (retentionPolicy != null) {
                this.retentionPolicy = retentionPolicy;
            }
            return this;
        }

        /**
         * Set the consistency level which is used for writing points.
         *
         * @param consistencyLevel the consistency level to set. It may be null.
         *                         If null than use default level {@link InfluxDB.ConsistencyLevel#ONE}.
         * @return {@code this}
         * @since 3.0.0
         */
        @Nonnull
        public Builder consistencyLevel(@Nullable final InfluxDB.ConsistencyLevel consistencyLevel) {

            if (consistencyLevel != null) {
                this.consistencyLevel = consistencyLevel;
            }
            return this;
        }

        /**
         * Set the default TimeUnit of the interval.
         *
         * @param precision the default TimeUnit of the interval. It may be null.
         *                  If null than use default level {@link TimeUnit#NANOSECONDS}.
         * @return {@code this}
         * @since 3.0.0
         */
        @Nonnull
        public Builder precision(@Nullable final TimeUnit precision) {

            if (precision != null) {
                this.precision = precision;
            }
            return this;
        }

        /**
         * Set the content type of HTTP request/response.
         *
         * @param mediaType the content type of HTTP request/response. It may be null.
         *                 If null than use default encoding {@code text/plain; charset=utf-8}.
         * @return {@code this}
         * @since 3.0.0
         */
        @Nonnull
        public Builder mediaType(@Nullable final MediaType mediaType) {
            if (mediaType != null) {
                this.mediaType = mediaType;
            }
            return this;
        }

        /**
         * Set the HTTP client to use for communication to InfluxDB.
         *
         * @param okHttpClient the HTTP client to use.
         * @return {@code this}
         * @since 3.0.0
         */
        @Nonnull
        public Builder okHttpClient(@Nonnull final OkHttpClient.Builder okHttpClient) {
            Objects.requireNonNull(okHttpClient, "OkHttpClient.Builder is required");
            this.okHttpClient = okHttpClient;
            return this;
        }

        /**
         * Adds custom listener to listen events from InfluxDB client.
         */
        public Builder addListener(@Nonnull final  InfluxDBEventListener eventListener) {
            this.listeners.add(eventListener);
            return this;
        }

        /**
         * Build an instance of InfluxDBOptions.
         *
         * @return {@code InfluxDBOptions}
         */
        @Nonnull
        public InfluxDBOptions build() {

            if (url == null || url.isEmpty()) {
                throw new IllegalStateException("The url to connect to InfluxDB has to be defined.");
            }

            return new InfluxDBOptions(this);
        }
    }
}
