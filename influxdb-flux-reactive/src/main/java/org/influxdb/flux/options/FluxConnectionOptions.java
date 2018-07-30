package org.influxdb.flux.options;

import okhttp3.OkHttpClient;
import org.influxdb.impl.Preconditions;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import javax.annotation.concurrent.ThreadSafe;
import java.util.Objects;

/**
 * FluxConnectionOptions are used to configure queries to the Flux.
 *
 * @author Jakub Bednar (bednar@github) (26/06/2018 08:59)
 * @since 3.0.0
 */
@ThreadSafe
public final class FluxConnectionOptions {

    private final String url;
    private final String orgID;
    private OkHttpClient.Builder okHttpClient;

    private FluxConnectionOptions(@Nonnull final Builder builder) {
        Objects.requireNonNull(builder, "FluxConnectionOptions.Builder is required");

        url = builder.url;
        orgID = builder.orgID;
        okHttpClient = builder.okHttpClient;
    }

    /**
     * @return the url to connect to Flux
     * @see FluxConnectionOptions.Builder#url(String)
     * @since 3.0.0
     */
    @Nonnull
    public String getUrl() {
        return url;
    }

    /**
     * @return the organization id required by Flux
     * @see FluxConnectionOptions.Builder#orgID(String)
     * @since 3.0.0
     */

    @Nonnull
    public String getOrgID() {
        return orgID;
    }

    /**
     * @return HTTP client to use for communication with Flux
     * @see FluxConnectionOptions.Builder#okHttpClient(OkHttpClient.Builder)
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
    public static FluxConnectionOptions.Builder builder() {
        return new FluxConnectionOptions.Builder();
    }

    /**
     * A builder for {@code FluxConnectionOptions}.
     *
     * @since 3.0.0
     */
    @NotThreadSafe
    public static class Builder {

        private String url;
        private String orgID;
        private OkHttpClient.Builder okHttpClient = new OkHttpClient.Builder();

        /**
         * Set the url to connect to Flux.
         *
         * @param url the url to connect to Flux. It must be defined.
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
         * Set the organization id required by Flux.
         *
         * @param orgID the organization id required by Flux. It must be defined.
         * @return {@code this}
         * @since 3.0.0
         */
        @Nonnull
        public Builder orgID(@Nonnull final String orgID) {
            Preconditions.checkNonEmptyString(orgID, "orgID");
            this.orgID = orgID;
            return this;
        }

        /**
         * Set the HTTP client to use for communication with Flux.
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
         * Build an instance of FluxConnectionOptions.
         *
         * @return {@link FluxConnectionOptions}
         */
        @Nonnull
        public FluxConnectionOptions build() {

            if (url == null || url.isEmpty()) {
                throw new IllegalStateException("The url to connect to Flux has to be defined.");
            }

            if (orgID == null || orgID.isEmpty()) {
                throw new IllegalStateException("The organization id required by Flux has to be defined.");
            }

            return new FluxConnectionOptions(this);
        }
    }
}
