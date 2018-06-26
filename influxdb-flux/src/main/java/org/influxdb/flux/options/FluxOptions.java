package org.influxdb.flux.options;

import org.influxdb.flux.Preconditions;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import javax.annotation.concurrent.ThreadSafe;
import java.util.Objects;

/**
 * FluxOptions are used to configure queries to the Flux.
 *
 * @author Jakub Bednar (bednar@github) (26/06/2018 08:59)
 */
@ThreadSafe
public final class FluxOptions {

    private final String url;
    private final String orgID;

    /**
     * Not defined FluxOptions.
     */
    public static final FluxOptions NOT_DEFINED = new FluxOptions();

    private FluxOptions() {
        url = null;
        orgID = null;
    }

    private FluxOptions(@Nonnull final Builder builder) {
        Objects.requireNonNull(builder, "FluxOptions.Builder is required");

        url = builder.url;
        orgID = builder.orgID;
    }

    /**
     * @return the url to connect to Flux
     * @see FluxOptions.Builder#url(String)
     * @since 3.0.0
     */
    @Nonnull
    public String getUrl() {
        return url;
    }

    /**
     * @return the organization id required by Flux
     * @see FluxOptions.Builder#orgID(String)
     * @since 3.0.0
     */

    @Nonnull
    public String getOrgID() {
        return orgID;
    }

    /**
     * @return {@link Boolean#TRUE} if FluxOptions are not defined.
     */
    boolean isNotDefined() {
        return url == null && orgID == null;
    }

    /**
     * Creates a builder instance.
     *
     * @return a builder
     * @since 3.0.0
     */
    @Nonnull
    public static FluxOptions.Builder builder() {
        return new FluxOptions.Builder();
    }

    /**
     * A builder for {@code FluxOptions}.
     *
     * @since 3.0.0
     */
    @NotThreadSafe
    public static class Builder {

        private String url;
        private String orgID;

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
         * Build an instance of InfluxDBOptions.
         *
         * @return {@code InfluxDBOptions}
         */
        @Nonnull
        public FluxOptions build() {

            if (url == null || url.isEmpty()) {
                throw new IllegalStateException("The url to connect to Flux has to be defined.");
            }

            if (orgID == null || orgID.isEmpty()) {
                throw new IllegalStateException("The organization id required by Flux has to be defined.");
            }

            return new FluxOptions(this);
        }
    }
}
