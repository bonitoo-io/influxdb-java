package org.influxdb.impl;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
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
import org.influxdb.flux.FluxReactive;
import org.influxdb.flux.events.AbstractFluxEvent;
import org.influxdb.flux.events.FluxErrorEvent;
import org.influxdb.flux.events.FluxSuccessEvent;
import org.influxdb.flux.mapper.FluxResult;
import org.influxdb.flux.mapper.FluxResultMapper;
import org.influxdb.flux.options.FluxCsvParserOptions;
import org.influxdb.flux.options.FluxOptions;
import org.influxdb.flux.options.FluxQueryOptions;
import org.reactivestreams.Publisher;
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
public class FluxReactiveImpl implements FluxReactive {

    private static final Logger LOG = Logger.getLogger(FluxReactiveImpl.class.getName());

    private final FluxResultMapper mapper = new FluxResultMapper();

    private final FluxOptions fluxOptions;
    private final FluxServiceReactive fluxService;
    private final HttpLoggingInterceptor loggingInterceptor;
    private final GzipRequestInterceptor gzipRequestInterceptor;

    private final PublishSubject<Object> eventPublisher;

    public FluxReactiveImpl(@Nonnull final FluxOptions fluxOptions) {

        this(fluxOptions, null);
    }

    FluxReactiveImpl(@Nonnull final FluxOptions fluxOptions, @Nullable final FluxServiceReactive fluxService) {

        Objects.requireNonNull(fluxOptions, "FluxOptions are required");

        this.fluxOptions = fluxOptions;

        this.loggingInterceptor = new HttpLoggingInterceptor();
        this.loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.NONE);
        this.gzipRequestInterceptor = new GzipRequestInterceptor();

        if (fluxService != null) {
            this.fluxService = fluxService;
        } else {

            OkHttpClient okHttpClient = fluxOptions.getOkHttpClient()
                    .addInterceptor(loggingInterceptor)
                    .addInterceptor(gzipRequestInterceptor)
                    .build();

            this.fluxService = new Retrofit.Builder()
                    .baseUrl(fluxOptions.getUrl())
                    .client(okHttpClient)
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build()
                    .create(FluxServiceReactive.class);
        }

        this.eventPublisher = PublishSubject.create();
    }

    @Override
    public <M> Flowable<M> flux(@Nonnull final Flux flux, final @Nonnull Class<M> measurementType) {

        Objects.requireNonNull(flux, "Flux is required");
        Objects.requireNonNull(measurementType, "Measurement type si required");

        return flux(flux, measurementType, FluxQueryOptions.DEFAULTS);
    }

    @Override
    public <M> Flowable<M> flux(@Nonnull final Flux flux,
                                @Nonnull final Class<M> measurementType,
                                @Nonnull final FluxQueryOptions queryOptions) {

        Objects.requireNonNull(flux, "Flux is required");
        Objects.requireNonNull(measurementType, "Measurement type si required");
        Objects.requireNonNull(queryOptions, "FluxQueryOptions are required");

        return flux(flux, new HashMap<>(), measurementType, queryOptions);
    }

    @Override
    public <M> Flowable<M> flux(@Nonnull final Flux flux,
                                @Nonnull final Map<String, Object> properties,
                                @Nonnull final Class<M> measurementType) {

        Objects.requireNonNull(flux, "Flux is required");
        Objects.requireNonNull(properties, "Properties are required");
        Objects.requireNonNull(measurementType, "Measurement type si required");

        return flux(flux, properties, measurementType, FluxQueryOptions.DEFAULTS);
    }

    @Override
    public <M> Flowable<M> flux(@Nonnull final Flux flux,
                                @Nonnull final Map<String, Object> properties,
                                @Nonnull final Class<M> measurementType,
                                @Nonnull final FluxQueryOptions queryOptions) {

        Objects.requireNonNull(flux, "Flux is required");
        Objects.requireNonNull(properties, "Properties are required");
        Objects.requireNonNull(measurementType, "Measurement type si required");
        Objects.requireNonNull(queryOptions, "FluxQueryOptions are required");

        return flux(Flowable.just(flux), properties, measurementType, queryOptions);
    }

    @Override
    public <M> Flowable<M> flux(@Nonnull final Publisher<Flux> fluxStream,
                                @Nonnull final Map<String, Object> properties,
                                @Nonnull final Class<M> measurementType) {

        Objects.requireNonNull(fluxStream, "Flux stream is required");
        Objects.requireNonNull(properties, "Parameters are required");
        Objects.requireNonNull(measurementType, "Measurement type si required");

        return flux(fluxStream, properties, measurementType, FluxQueryOptions.DEFAULTS);
    }

    @Override
    public <M> Flowable<M> flux(@Nonnull final Publisher<Flux> fluxStream,
                                @Nonnull final Map<String, Object> properties,
                                @Nonnull final Class<M> measurementType,
                                @Nonnull final FluxQueryOptions queryOptions) {

        Objects.requireNonNull(fluxStream, "Flux stream is required");
        Objects.requireNonNull(properties, "Properties are required");
        Objects.requireNonNull(measurementType, "Measurement type si required");
        Objects.requireNonNull(queryOptions, "FluxQueryOptions are required");

        return flux(fluxStream, properties, queryOptions)
                .map(fluxResults -> mapper.toPOJO(fluxResults, measurementType))
                .concatMap(Flowable::fromIterable);
    }

    @Override
    public Flowable<FluxResult> flux(@Nonnull final Flux flux) {

        Objects.requireNonNull(flux, "Flux is required");

        return flux(flux, FluxQueryOptions.DEFAULTS);
    }

    @Override
    public Flowable<FluxResult> flux(@Nonnull final Flux flux, @Nonnull final FluxQueryOptions queryOptions) {

        Objects.requireNonNull(flux, "Flux is required");
        Objects.requireNonNull(queryOptions, "FluxQueryOptions are required");

        return flux(flux, new HashMap<>(), queryOptions);
    }

    @Override
    public Flowable<FluxResult> flux(@Nonnull final Flux flux, @Nonnull final Map<String, Object> properties) {

        Objects.requireNonNull(flux, "Flux is required");
        Objects.requireNonNull(properties, "Parameters are required");

        return flux(flux, properties, FluxQueryOptions.DEFAULTS);
    }

    @Override
    public Flowable<FluxResult> flux(@Nonnull final Flux flux,
                                     @Nonnull final Map<String, Object> properties,
                                     @Nonnull final FluxQueryOptions queryOptions) {

        Objects.requireNonNull(flux, "Flux is required");
        Objects.requireNonNull(properties, "Parameters are required");
        Objects.requireNonNull(queryOptions, "FluxQueryOptions are required");

        return flux(Flowable.just(flux), properties, queryOptions);
    }

    @Override
    public Flowable<FluxResult> flux(@Nonnull final Publisher<Flux> fluxStream,
                                     @Nonnull final Map<String, Object> properties) {
        Objects.requireNonNull(fluxStream, "Flux stream is required");
        Objects.requireNonNull(properties, "Parameters are required");

        return flux(fluxStream, properties, FluxQueryOptions.DEFAULTS);
    }

    @Override
    public Flowable<FluxResult> flux(@Nonnull final Publisher<Flux> fluxStream,
                                     @Nonnull final Map<String, Object> properties,
                                     @Nonnull final FluxQueryOptions queryOptions) {

        Objects.requireNonNull(fluxStream, "Flux stream is required");
        Objects.requireNonNull(properties, "Parameters are required");
        Objects.requireNonNull(queryOptions, "FluxQueryOptions are required");

        return Flowable.fromPublisher(fluxStream).concatMap((Function<Flux, Publisher<FluxResult>>) flux -> {

            //
            // Parameters
            //
            String orgID = this.fluxOptions.getOrgID();
            String query = flux.print(new FluxChain().addParameters(properties));

            return fluxService
                    .query(query, orgID)
                    .flatMap(
                            // success response
                            body -> chunkReader(query, this.fluxOptions, body, queryOptions.getParserOptions()),
                            // error response
                            throwable -> (observer -> {

                                InfluxDBException influxDBException = InfluxDBException
                                        .buildExceptionForThrowable(throwable);

                                // publish event
                                publishEvent(new FluxErrorEvent(fluxOptions, query, influxDBException));
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
    public FluxReactive enableGzip() {
        this.gzipRequestInterceptor.enable();
        return this;
    }

    @Nonnull
    @Override
    public FluxReactive disableGzip() {
        this.gzipRequestInterceptor.disable();
        return this;
    }

    @Override
    public boolean isGzipEnabled() {
        return this.gzipRequestInterceptor.isEnabled();
    }

    @Nonnull
    @Override
    public HttpLoggingInterceptor.Level getLogLevel() {
        return this.loggingInterceptor.getLevel();
    }

    @Nonnull
    @Override
    public FluxReactive setLogLevel(@Nonnull final HttpLoggingInterceptor.Level logLevel) {

        Objects.requireNonNull(logLevel, "Log level is required");

        this.loggingInterceptor.setLevel(logLevel);

        return this;
    }

    @Nonnull
    @Override
    public FluxReactive close() {

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
                                               @Nonnull final FluxOptions options,
                                               @Nonnull final ResponseBody body,
                                               @Nonnull final FluxCsvParserOptions parserOptions) {

        Objects.requireNonNull(options, "FluxOptions are required");
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
