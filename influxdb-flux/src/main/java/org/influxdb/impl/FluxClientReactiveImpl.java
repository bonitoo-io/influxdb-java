package org.influxdb.impl;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.functions.Function;
import io.reactivex.subjects.PublishSubject;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import okio.BufferedSource;
import org.influxdb.InfluxDBException;
import org.influxdb.flux.Flux;
import org.influxdb.flux.FluxChain;
import org.influxdb.flux.FluxClientReactive;
import org.influxdb.flux.events.AbstractFluxEvent;
import org.influxdb.flux.events.FluxErrorEvent;
import org.influxdb.flux.events.FluxSuccessEvent;
import org.influxdb.flux.mapper.FluxResult;
import org.influxdb.flux.mapper.FluxResultMapper;
import org.influxdb.flux.options.FluxConnectionOptions;
import org.influxdb.flux.options.FluxCsvParserOptions;
import org.influxdb.flux.options.FluxOptions;
import org.reactivestreams.Publisher;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.EOFException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Jakub Bednar (bednar@github) (26/06/2018 11:59)
 */
public class FluxClientReactiveImpl implements FluxClientReactive {

    private static final Logger LOG = Logger.getLogger(FluxClientReactiveImpl.class.getName());

    private final FluxResultMapper mapper = new FluxResultMapper();

    private final FluxConnectionOptions fluxConnectionOptions;
    private final FluxServiceReactive fluxService;
    private final HttpLoggingInterceptor loggingInterceptor;
    private final GzipRequestInterceptor gzipRequestInterceptor;

    private final PublishSubject<Object> eventPublisher;

    public FluxClientReactiveImpl(@Nonnull final FluxConnectionOptions fluxConnectionOptions) {

        this(fluxConnectionOptions, null);
    }

    FluxClientReactiveImpl(@Nonnull final FluxConnectionOptions fluxConnectionOptions,
                           @Nullable final FluxServiceReactive fluxService) {

        Objects.requireNonNull(fluxConnectionOptions, "FluxConnectionOptions are required");

        this.fluxConnectionOptions = fluxConnectionOptions;

        this.loggingInterceptor = new HttpLoggingInterceptor();
        this.loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.NONE);
        this.gzipRequestInterceptor = new GzipRequestInterceptor();

        if (fluxService != null) {
            this.fluxService = fluxService;
        } else {

            OkHttpClient okHttpClient = fluxConnectionOptions.getOkHttpClient()
                    .addInterceptor(loggingInterceptor)
                    .addInterceptor(gzipRequestInterceptor)
                    .build();

            this.fluxService = new Retrofit.Builder()
                    .baseUrl(fluxConnectionOptions.getUrl())
                    .client(okHttpClient)
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build()
                    .create(FluxServiceReactive.class);
        }

        this.eventPublisher = PublishSubject.create();
    }

    @Override
    public <M> Flowable<M> flux(@Nonnull final Flux query, final @Nonnull Class<M> measurementType) {

        Objects.requireNonNull(query, "Flux is required");
        Objects.requireNonNull(measurementType, "Measurement type si required");

        return flux(query, measurementType, FluxOptions.DEFAULTS);
    }

    @Override
    public <M> Flowable<M> flux(@Nonnull final Flux query,
                                @Nonnull final Class<M> measurementType,
                                @Nonnull final FluxOptions options) {

        Objects.requireNonNull(query, "Flux is required");
        Objects.requireNonNull(measurementType, "Measurement type si required");
        Objects.requireNonNull(options, "FluxOptions are required");

        return flux(query, new HashMap<>(), measurementType, options);
    }

    @Override
    public <M> Flowable<M> flux(@Nonnull final Flux query,
                                @Nonnull final Map<String, Object> properties,
                                @Nonnull final Class<M> measurementType) {

        Objects.requireNonNull(query, "Flux is required");
        Objects.requireNonNull(properties, "Properties are required");
        Objects.requireNonNull(measurementType, "Measurement type si required");

        return flux(query, properties, measurementType, FluxOptions.DEFAULTS);
    }

    @Override
    public <M> Flowable<M> flux(@Nonnull final Flux query,
                                @Nonnull final Map<String, Object> properties,
                                @Nonnull final Class<M> measurementType,
                                @Nonnull final FluxOptions options) {

        Objects.requireNonNull(query, "Flux is required");
        Objects.requireNonNull(properties, "Properties are required");
        Objects.requireNonNull(measurementType, "Measurement type si required");
        Objects.requireNonNull(options, "FluxOptions are required");

        return flux(Flowable.just(query), properties, measurementType, options);
    }

    @Override
    public <M> Flowable<M> flux(@Nonnull final Publisher<Flux> queryStream,
                                @Nonnull final Map<String, Object> properties,
                                @Nonnull final Class<M> measurementType) {

        Objects.requireNonNull(queryStream, "Flux stream is required");
        Objects.requireNonNull(properties, "Parameters are required");
        Objects.requireNonNull(measurementType, "Measurement type si required");

        return flux(queryStream, properties, measurementType, FluxOptions.DEFAULTS);
    }

    @Override
    public <M> Flowable<M> flux(@Nonnull final Publisher<Flux> queryStream,
                                @Nonnull final Map<String, Object> properties,
                                @Nonnull final Class<M> measurementType,
                                @Nonnull final FluxOptions options) {

        Objects.requireNonNull(queryStream, "Flux stream is required");
        Objects.requireNonNull(properties, "Properties are required");
        Objects.requireNonNull(measurementType, "Measurement type si required");
        Objects.requireNonNull(options, "FluxOptions are required");

        return flux(queryStream, properties, options)
                .map(fluxResults -> mapper.toPOJO(fluxResults, measurementType))
                .concatMap(Flowable::fromIterable);
    }

    @Override
    public Flowable<FluxResult> flux(@Nonnull final Flux query) {

        Objects.requireNonNull(query, "Flux is required");

        return flux(query, FluxOptions.DEFAULTS);
    }

    @Override
    public Flowable<FluxResult> flux(@Nonnull final Flux query, @Nonnull final FluxOptions options) {

        Objects.requireNonNull(query, "Flux is required");
        Objects.requireNonNull(options, "FluxOptions are required");

        return flux(query, new HashMap<>(), options);
    }

    @Override
    public Flowable<FluxResult> flux(@Nonnull final Flux query, @Nonnull final Map<String, Object> properties) {

        Objects.requireNonNull(query, "Flux is required");
        Objects.requireNonNull(properties, "Parameters are required");

        return flux(query, properties, FluxOptions.DEFAULTS);
    }

    @Override
    public Flowable<FluxResult> flux(@Nonnull final Flux query,
                                     @Nonnull final Map<String, Object> properties,
                                     @Nonnull final FluxOptions options) {

        Objects.requireNonNull(query, "Flux is required");
        Objects.requireNonNull(properties, "Parameters are required");
        Objects.requireNonNull(options, "FluxOptions are required");

        return flux(Flowable.just(query), properties, options);
    }

    @Override
    public Flowable<FluxResult> flux(@Nonnull final Publisher<Flux> queryStream,
                                     @Nonnull final Map<String, Object> properties) {
        Objects.requireNonNull(queryStream, "Flux stream is required");
        Objects.requireNonNull(properties, "Parameters are required");

        return flux(queryStream, properties, FluxOptions.DEFAULTS);
    }

    @Override
    public Flowable<FluxResult> flux(@Nonnull final Publisher<Flux> queryStream,
                                     @Nonnull final Map<String, Object> properties,
                                     @Nonnull final FluxOptions options) {

        Objects.requireNonNull(queryStream, "Flux stream is required");
        Objects.requireNonNull(properties, "Parameters are required");
        Objects.requireNonNull(options, "FluxOptions are required");

        return Flowable.fromPublisher(queryStream).concatMap((Function<Flux, Publisher<FluxResult>>) flux -> {

            //
            // Parameters
            //
            String orgID = this.fluxConnectionOptions.getOrgID();
            String query = flux.print(new FluxChain().addParameters(properties));

            return fluxService
                    .query(query, orgID)
                    .flatMap(
                            // success response
                            body -> chunkReader(query, this.fluxConnectionOptions, body, options.getParserOptions()),
                            // error response
                            throwable -> (observer -> {

                                InfluxDBException influxDBException = InfluxDBException
                                        .buildExceptionForThrowable(throwable);

                                // publish event
                                publishEvent(new FluxErrorEvent(fluxConnectionOptions, query, influxDBException));
                                observer.onError(influxDBException);
                            }),
                            // end of response
                            Observable::empty)
                    .toFlowable(BackpressureStrategy.BUFFER);
        });
    }

    @Nonnull
    @Override
    public <T extends AbstractFluxEvent> Observable<T> listenEvents(@Nonnull final Class<T> eventType) {

        Objects.requireNonNull(eventType, "EventType is required");

        return eventPublisher.ofType(eventType);
    }

    @Nonnull
    @Override
    public FluxClientReactive enableGzip() {
        this.gzipRequestInterceptor.enable();
        return this;
    }

    @Nonnull
    @Override
    public FluxClientReactive disableGzip() {
        this.gzipRequestInterceptor.disable();
        return this;
    }

    @Override
    public boolean isGzipEnabled() {
        return this.gzipRequestInterceptor.isEnabled();
    }

    @Override
    @Nonnull
    public Maybe<Boolean> ping() {

        return fluxService
                .ping()
                .map(Response::isSuccessful);
    }

    @Nonnull
    @Override
    public HttpLoggingInterceptor.Level getLogLevel() {
        return this.loggingInterceptor.getLevel();
    }

    @Nonnull
    @Override
    public FluxClientReactive setLogLevel(@Nonnull final HttpLoggingInterceptor.Level logLevel) {

        Objects.requireNonNull(logLevel, "Log level is required");

        this.loggingInterceptor.setLevel(logLevel);

        return this;
    }

    @Nonnull
    @Override
    public FluxClientReactive close() {

        LOG.log(Level.INFO, "Dispose all event listeners before shutdown.");

        eventPublisher.onComplete();

        return this;
    }

    @Override
    public boolean isClosed() {
        return eventPublisher.hasComplete();
    }

    @Nonnull
    private Observable<FluxResult> chunkReader(@Nonnull final String query,
                                               @Nonnull final FluxConnectionOptions options,
                                               @Nonnull final ResponseBody body,
                                               @Nonnull final FluxCsvParserOptions parserOptions) {

        Objects.requireNonNull(options, "FluxConnectionOptions are required");
        Preconditions.checkNonEmptyString(query, "Flux query");
        Objects.requireNonNull(body, "ResponseBody is required");
        Objects.requireNonNull(parserOptions, "FluxCsvParserOptions are required");

        return Observable.create(subscriber -> {

            boolean isCompleted = false;
            try {
                BufferedSource source = body.source();

                //
                // Subscriber is not disposed && source has data => parse
                //
                while (!subscriber.isDisposed() && !source.exhausted()) {

                    FluxResult fluxResult = mapper.toFluxResult(source, parserOptions);
                    if (fluxResult != null) {

                        subscriber.onNext(fluxResult);
                        publishEvent(new FluxSuccessEvent(options, query));
                    }
                }
            } catch (IOException e) {

                //
                // Socket close by remote server or end of data
                //
                if (e.getMessage().equals("Socket closed") || e instanceof EOFException) {
                    isCompleted = true;
                    subscriber.onComplete();
                } else {
                    throw new UncheckedIOException(e);
                }
            }

            //if response end we get here
            if (!isCompleted) {
                subscriber.onComplete();
            }

            body.close();
        });
    }

    private <T extends AbstractFluxEvent> void publishEvent(@Nonnull final T event) {

        Objects.requireNonNull(event, "Event is required");

        event.logEvent();
        eventPublisher.onNext(event);
    }
}
