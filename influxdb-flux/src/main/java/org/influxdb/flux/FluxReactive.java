package org.influxdb.flux;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import okhttp3.logging.HttpLoggingInterceptor;
import org.influxdb.flux.events.AbstractFluxEvent;
import org.influxdb.flux.mapper.FluxResult;
import org.reactivestreams.Publisher;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * The client for the Flux service.
 *
 * @author Jakub Bednar (bednar@github) (26/06/2018 11:07)
 * @since 3.0.0
 */
public interface FluxReactive {

    /**
     * Execute a Flux against the Flux service.
     *
     * @param flux            the flux query to execute
     * @param measurementType The type of the measurement (POJO)
     * @param <M>             The type of the measurement (POJO)
     * @return {@link Flowable} emitting a {@link FluxResult} mapped to {@code measurementType} which are matched
     * the query or {@link Flowable#empty()} if none found.
     */
    <M> Flowable<M> flux(@Nonnull final Flux flux, @Nonnull final Class<M> measurementType);

    /**
     * Execute a Flux against the Flux service.
     *
     * @param flux            the flux query to execute
     * @param parameters      named parameters
     * @param measurementType The type of the measurement (POJO)
     * @param <M>             The type of the measurement (POJO)
     * @return {@link Flowable} emitting a {@link FluxResult} mapped to {@code measurementType} which are matched
     * the query or {@link Flowable#empty()} if none found.
     */
    <M> Flowable<M> flux(@Nonnull final Flux flux,
                         @Nonnull final Map<String, Object> parameters,
                         @Nonnull final Class<M> measurementType);

    /**
     * Execute a Flux against the Flux service.
     *
     * @param fluxStream      the flux query to execute
     * @param properties      named properties
     * @param measurementType The type of the measurement (POJO)
     * @param <M>             The type of the measurement (POJO)
     * @return {@link Flowable} emitting a {@link FluxResult} mapped to {@code measurementType} which are matched
     * the query or {@link Flowable#empty()} if none found.
     */
    <M> Flowable<M> flux(@Nonnull final Publisher<Flux> fluxStream,
                         @Nonnull final Map<String, Object> properties,
                         @Nonnull final Class<M> measurementType);

    /**
     * Execute a Flux against the Flux service.
     *
     * @param flux the flux query to execute
     * @return {@link Flowable} emitting a {@link FluxResult} which are matched the query or
     * {@link Flowable#empty()} if none found.
     */
    Flowable<FluxResult> flux(@Nonnull final Flux flux);

    /**
     * Execute a Flux against the Flux service.
     *
     * @param flux       the flux query to execute
     * @param properties named properties
     * @return {@link Flowable} emitting a {@link FluxResult} which are matched the query or
     * {@link Flowable#empty()} if none found.
     */
    Flowable<FluxResult> flux(@Nonnull final Flux flux, @Nonnull final Map<String, Object> properties);

    /**
     * Execute a Flux against the Flux service.
     *
     * @param fluxStream the flux query to execute
     * @param properties named properties
     * @return {@link Flowable} emitting a {@link FluxResult} which are matched the query or
     * {@link Flowable#empty()} if none found.
     */
    Flowable<FluxResult> flux(@Nonnull final Publisher<Flux> fluxStream, @Nonnull final Map<String, Object> properties);

    /**
     * Listen the events produced by {@link FluxReactive}.
     *
     * @param eventType type of event to listen
     * @param <T>       type of event to listen
     * @return lister for {@code eventType} events
     */
    @Nonnull
    <T extends AbstractFluxEvent> Observable<T> listenEvents(@Nonnull Class<T> eventType);

    /**
     * Enable Gzip compress for http request body.
     *
     * @return the FluxReactive instance to be able to use it in a fluent manner.
     */
    @Nonnull
    FluxReactive enableGzip();

    /**
     * Disable Gzip compress for http request body.
     *
     * @return the FluxReactive instance to be able to use it in a fluent manner.
     */
    @Nonnull
    FluxReactive disableGzip();

    /**
     * Returns whether Gzip compress for http request body is enabled.
     *
     * @return true if gzip is enabled.
     */
    boolean isGzipEnabled();

    /**
     * @return the {@link HttpLoggingInterceptor.Level} that is used for logging requests and responses
     */
    @Nonnull
    HttpLoggingInterceptor.Level getLogLevel();

    /**
     * Set the log level for the request and response information.
     *
     * @param logLevel the log level to set.
     * @return the FluxReactive instance to be able to use it in a fluent manner.
     */
    @Nonnull
    FluxReactive setLogLevel(@Nonnull final HttpLoggingInterceptor.Level logLevel);

    /**
     * Dispose all event listeners before shutdown.
     *
     * @return the FluxReactive instance to be able to use it in a fluent manner.
     */
    @Nonnull
    FluxReactive close();

    /**
     * @return {@link Boolean#TRUE} if all listeners are disposed
     */
    boolean isClosed();
}
