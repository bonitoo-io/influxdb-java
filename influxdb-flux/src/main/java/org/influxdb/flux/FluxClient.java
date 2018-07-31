package org.influxdb.flux;

import okhttp3.logging.HttpLoggingInterceptor;
import org.influxdb.flux.events.AbstractFluxEvent;
import org.influxdb.flux.mapper.FluxResult;
import org.influxdb.flux.options.FluxOptions;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * The client for the Flux service.
 *
 * @author Jakub Bednar (bednar@github) (30/07/2018 10:55)
 * @since 3.0.0
 */
public interface FluxClient {

    /**
     * Execute a Flux against the Flux service.
     *
     * @param query           the flux query to execute
     * @param measurementType the type of the measurement (POJO)
     * @param <M>             the type of the measurement (POJO)
     * @return {@link List} of {@code measurementType} which are matched the query or empty list if none found.
     */
    @Nonnull
    <M> List<M> flux(@Nonnull final Flux query, @Nonnull final Class<M> measurementType);

    /**
     * Execute a Flux against the Flux service.
     *
     * @param query           the flux query to execute
     * @param measurementType the type of the measurement (POJO)
     * @param <M>             the type of the measurement (POJO)
     * @param callback        callback to consume result which are matched the query
     */
    <M> void flux(@Nonnull final Flux query,
                  @Nonnull final Class<M> measurementType,
                  @Nonnull final Consumer<List<M>> callback);

    /**
     * Execute a Flux against the Flux service.
     *
     * @param query           the flux query to execute
     * @param measurementType the type of the measurement (POJO)
     * @param <M>             the type of the measurement (POJO)
     * @param options         the options for the query
     * @return {@link List} of {@code measurementType} which are matched the query or empty list if none found.
     */
    @Nonnull
    <M> List<M> flux(@Nonnull final Flux query,
                     @Nonnull final Class<M> measurementType,
                     @Nonnull final FluxOptions options);

    /**
     * Execute a Flux against the Flux service.
     *
     * @param query           the flux query to execute
     * @param measurementType the type of the measurement (POJO)
     * @param <M>             the type of the measurement (POJO)
     * @param options         the options for the query
     * @param callback        callback to consume result which are matched the query
     */
    <M> void flux(@Nonnull final Flux query,
                  @Nonnull final Class<M> measurementType,
                  @Nonnull final FluxOptions options,
                  @Nonnull final Consumer<List<M>> callback);

    /**
     * Execute a Flux against the Flux service.
     *
     * @param query           the flux query to execute
     * @param properties      named properties
     * @param measurementType the type of the measurement (POJO)
     * @param <M>             the type of the measurement (POJO)
     * @return {@link List} of {@code measurementType} which are matched the query or empty list if none found.
     */
    @Nonnull
    <M> List<M> flux(@Nonnull final Flux query,
                     @Nonnull final Map<String, Object> properties,
                     @Nonnull final Class<M> measurementType);

    /**
     * Execute a Flux against the Flux service.
     *
     * @param query           the flux query to execute
     * @param properties      named properties
     * @param measurementType the type of the measurement (POJO)
     * @param <M>             the type of the measurement (POJO)
     * @param callback        callback to consume result which are matched the query
     */
    <M> void flux(@Nonnull final Flux query,
                  @Nonnull final Map<String, Object> properties,
                  @Nonnull final Class<M> measurementType, @Nonnull final Consumer<List<M>> callback);

    /**
     * Execute a Flux against the Flux service.
     *
     * @param query           the flux query to execute
     * @param properties      named properties
     * @param measurementType the type of the measurement (POJO)
     * @param <M>             the type of the measurement (POJO)
     * @param options         the options for the query
     * @return {@link List} of {@code measurementType} which are matched the query or empty list if none found.
     */
    @Nonnull
    <M> List<M> flux(@Nonnull final Flux query,
                     @Nonnull final Map<String, Object> properties,
                     @Nonnull final Class<M> measurementType,
                     @Nonnull final FluxOptions options);

    /**
     * Execute a Flux against the Flux service.
     *
     * @param query           the flux query to execute
     * @param properties      named properties
     * @param measurementType the type of the measurement (POJO)
     * @param <M>             the type of the measurement (POJO)
     * @param options         the options for the query
     * @param callback        callback to consume result which are matched the query
     */
    <M> void flux(@Nonnull final Flux query,
                  @Nonnull final Map<String, Object> properties,
                  @Nonnull final Class<M> measurementType,
                  @Nonnull final FluxOptions options, @Nonnull final Consumer<List<M>> callback);


    /**
     * Execute a Flux against the Flux service.
     *
     * @param query the flux query to execute
     * @return {@link FluxResult}  which are matched the query
     */
    @Nonnull
    FluxResult flux(@Nonnull final Flux query);

    /**
     * Execute a Flux against the Flux service.
     *
     * @param query    the flux query to execute
     * @param callback callback to consume result which are matched the query
     */
    void flux(@Nonnull final Flux query, @Nonnull final Consumer<FluxResult> callback);

    /**
     * Execute a Flux against the Flux service.
     *
     * @param query   the flux query to execute
     * @param options the options for the query
     * @return {@link FluxResult}  which are matched the query
     */
    @Nonnull
    FluxResult flux(@Nonnull final Flux query, @Nonnull final FluxOptions options);

    /**
     * Execute a Flux against the Flux service.
     *
     * @param query    the flux query to execute
     * @param options  the options for the query
     * @param callback callback to consume result which are matched the query
     */
    void flux(@Nonnull final Flux query,
              @Nonnull final FluxOptions options,
              @Nonnull final Consumer<FluxResult> callback);

    /**
     * Execute a Flux against the Flux service.
     *
     * @param query      the flux query to execute
     * @param properties named properties
     * @return {@link FluxResult}  which are matched the query
     */
    @Nonnull
    FluxResult flux(@Nonnull final Flux query, @Nonnull final Map<String, Object> properties);

    /**
     * Execute a Flux against the Flux service.
     *
     * @param query      the flux query to execute
     * @param properties named properties
     * @param callback   callback to consume result which are matched the query
     */
    void flux(@Nonnull final Flux query,
              @Nonnull final Map<String, Object> properties,
              @Nonnull final Consumer<FluxResult> callback);

    /**
     * Execute a Flux against the Flux service.
     *
     * @param query      the flux query to execute
     * @param properties named properties
     * @param options    the options for the query
     * @return {@link FluxResult}  which are matched the query
     */
    @Nonnull
    FluxResult flux(@Nonnull final Flux query,
                    @Nonnull final Map<String, Object> properties,
                    @Nonnull final FluxOptions options);

    /**
     * Execute a Flux against the Flux service.
     *
     * @param query      the flux query to execute
     * @param properties named properties
     * @param options    the options for the query
     * @param callback   callback to consume result which are matched the query
     */
    void flux(@Nonnull final Flux query,
              @Nonnull final Map<String, Object> properties,
              @Nonnull final FluxOptions options,
              @Nonnull final Consumer<FluxResult> callback);

    /**
     * Listen the events produced by {@link FluxClient}.
     *
     * @param eventType type of event to listen
     * @param <T>       type of event to listen
     * @param listener  listener to consume events
     */
    <T extends AbstractFluxEvent> void subscribeEvents(@Nonnull final Class<T> eventType,
                                                       @Nonnull final Consumer<T> listener);

    /**
     * Listen the events produced by {@link FluxClient}.
     *
     * @param <T>      type of event to listen
     * @param listener listener to unsubscribe to events
     */
    <T extends AbstractFluxEvent> void unsubscribeEvents(@Nonnull final Consumer<T> listener);

    /**
     * Enable Gzip compress for http request body.
     *
     * @return the FluxClientReactive instance to be able to use it in a fluent manner.
     */
    @Nonnull
    FluxClient enableGzip();

    /**
     * Disable Gzip compress for http request body.
     *
     * @return the FluxClientReactive instance to be able to use it in a fluent manner.
     */
    @Nonnull
    FluxClient disableGzip();

    /**
     * Returns whether Gzip compress for http request body is enabled.
     *
     * @return true if gzip is enabled.
     */
    boolean isGzipEnabled();

    /**
     * Check the status of Flux Server.
     *
     * @return {@link Boolean#TRUE} if server is healthy otherwise return {@link Boolean#FALSE}
     */
    @Nonnull
    Boolean ping();

    /**
     * @return the {@link HttpLoggingInterceptor.Level} that is used for logging requests and responses
     */
    @Nonnull
    HttpLoggingInterceptor.Level getLogLevel();

    /**
     * Set the log level for the request and response information.
     *
     * @param logLevel the log level to set.
     * @return the FluxClientReactive instance to be able to use it in a fluent manner.
     */
    @Nonnull
    FluxClient setLogLevel(@Nonnull final HttpLoggingInterceptor.Level logLevel);

    /**
     * Dispose all event listeners before shutdown.
     *
     * @return the FluxClientReactive instance to be able to use it in a fluent manner.
     */
    @Nonnull
    FluxClient close();
}
