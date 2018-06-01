package org.influxdb;

import okhttp3.OkHttpClient;
import org.influxdb.impl.Preconditions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import java.util.Objects;

/**
 * Various settings to control the behavior of a {@code InfluxDB}.
 *
 * @author Jakub Bednar (bednar@github) (01/06/2018 07:53)
 * @see InfluxDB
 * @see InfluxDBFactory
 * @since 3.0.0
 */
public final class InfluxDBOptions {

    private String url;

    private String username;
    private String password;

    private OkHttpClient.Builder okHttpClient;

    private InfluxDBOptions(@Nonnull final Builder builder) {

        Objects.requireNonNull(builder, "InfluxDBOptions.Builder is required");

        url = builder.url;

        username = builder.username;
        password = builder.password;

        okHttpClient = builder.okHttpClient;
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

        private OkHttpClient.Builder okHttpClient = new OkHttpClient.Builder();

        /**
         * Set the url to connect to InfluxDB.
         *
         * @param url the url to connect to InfluxDB. Is must be defined.
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
