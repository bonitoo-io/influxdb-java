package org.influxdb.impl;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableTransformer;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okio.BufferedSource;
import org.influxdb.InfluxDBException;
import org.influxdb.InfluxDBOptions;
import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;
import org.influxdb.dto.BoundParameterQuery;
import org.influxdb.dto.Point;
import org.influxdb.dto.Pong;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.influxdb.reactive.InfluxDBReactive;
import org.influxdb.reactive.event.AbstractInfluxEvent;
import org.influxdb.reactive.event.BackpressureEvent;
import org.influxdb.reactive.event.QueryParsedResponseEvent;
import org.influxdb.reactive.event.UnhandledErrorEvent;
import org.influxdb.reactive.event.WriteErrorEvent;
import org.influxdb.reactive.event.WriteSuccessEvent;
import org.influxdb.reactive.option.BatchOptionsReactive;
import org.influxdb.reactive.option.QueryOptions;
import org.influxdb.reactive.option.WriteOptions;
import org.reactivestreams.Publisher;
import retrofit2.HttpException;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.EOFException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author Jakub Bednar (bednar@github) (01/06/2018 10:37)
 */
public class InfluxDBReactiveImpl extends AbstractInfluxDB<InfluxDBServiceReactive> implements InfluxDBReactive {

    private static final Logger LOG = Logger.getLogger(InfluxDBReactiveImpl.class.getName());

    private final PublishProcessor<DataPoint> processor;
    private final PublishSubject<Object> eventPublisher;
    private final Disposable writeConsumer;

    private final InfluxDBOptions options;
    private final BatchOptionsReactive batchOptions;
    private final WriteOptions defaultWriteOptions;
    private final InfluxDBResultMapper resultMapper;

    public InfluxDBReactiveImpl(@Nonnull final InfluxDBOptions options) {
        this(options, BatchOptionsReactive.DEFAULTS);
    }


    public InfluxDBReactiveImpl(@Nonnull final InfluxDBOptions options,
                                @Nonnull final BatchOptionsReactive batchOptions) {

        this(options, batchOptions, Schedulers.newThread(), Schedulers.computation(), Schedulers.trampoline(),
                Schedulers.trampoline(), null);
    }

    InfluxDBReactiveImpl(@Nonnull final InfluxDBOptions options,
                         @Nonnull final BatchOptionsReactive batchOptions,
                         @Nonnull final Scheduler processorScheduler,
                         @Nonnull final Scheduler batchScheduler,
                         @Nonnull final Scheduler jitterScheduler,
                         @Nonnull final Scheduler retryScheduler,
                         @Nullable final InfluxDBServiceReactive influxDBService) {

        super(InfluxDBServiceReactive.class, options, influxDBService, null);

        Preconditions.checkNonEmptyString(options.getDatabase(), "Default database");

        this.eventPublisher = PublishSubject.create();

        this.options = options;
        this.batchOptions = batchOptions;
        this.defaultWriteOptions = WriteOptions.builder()
                .database(options.getDatabase())
                .consistencyLevel(options.getConsistencyLevel())
                .retentionPolicy(options.getRetentionPolicy())
                .precision(options.getPrecision())
                .build();

        this.resultMapper = new InfluxDBResultMapper();

        this.processor = PublishProcessor.create();
        this.writeConsumer = this.processor
                //
                // Backpressure
                //
                .onBackpressureBuffer(
                        batchOptions.getBufferLimit(),
                        () -> publish(new BackpressureEvent()),
                        batchOptions.getBackpressureStrategy())
                .observeOn(processorScheduler)
                //
                // Batching
                //
                .window(batchOptions.getFlushInterval(),
                        TimeUnit.MILLISECONDS,
                        batchScheduler,
                        batchOptions.getActions(),
                        true)
                //
                // Jitter interval
                //
                .compose(applyJitter(jitterScheduler))
                .doOnError(throwable -> publish(new UnhandledErrorEvent(throwable)))
                .subscribe(new WritePointsConsumer(retryScheduler));
    }

    @Override
    protected void configure(@Nonnull final Retrofit.Builder builder) {
        builder.addCallAdapterFactory(RxJava2CallAdapterFactory.create());
    }

    @Override
    public <M> Maybe<M> writeMeasurement(@Nonnull final M measurement) {

        Objects.requireNonNull(measurement, "Measurement is required");

        return writeMeasurement(measurement, defaultWriteOptions);
    }

    @Override
    public <M> Maybe<M> writeMeasurement(@Nonnull final M measurement, @Nonnull final WriteOptions options) {

        Objects.requireNonNull(measurement, "Measurement is required");
        Objects.requireNonNull(options, "WriteOptions is required");

        return writeMeasurements(Flowable.just(measurement), options).firstElement();
    }

    @Override
    public <M> Flowable<M> writeMeasurements(@Nonnull final Iterable<M> measurements) {

        Objects.requireNonNull(measurements, "Measurements are required");

        return writeMeasurements(measurements, defaultWriteOptions);
    }

    @Override
    public <M> Flowable<M> writeMeasurements(@Nonnull final Iterable<M> measurements,
                                             @Nonnull final WriteOptions options) {

        Objects.requireNonNull(measurements, "Measurements are required");
        Objects.requireNonNull(options, "WriteOptions is required");

        return writeMeasurements(Flowable.fromIterable(measurements), options);
    }

    @Override
    public <M> Flowable<M> writeMeasurements(@Nonnull final Publisher<M> measurementStream) {

        Objects.requireNonNull(measurementStream, "Measurement stream is required");

        return writeMeasurements(measurementStream, defaultWriteOptions);
    }

    @Override
    public <M> Flowable<M> writeMeasurements(@Nonnull final Publisher<M> measurementStream,
                                             @Nonnull final WriteOptions options) {

        Objects.requireNonNull(measurementStream, "Measurement stream is required");
        Objects.requireNonNull(options, "WriteOptions is required");

        Flowable<M> emitting = Flowable.fromPublisher(measurementStream);
        writePoints(emitting.map(new MeasurementToPoint<>()), options);

        return emitting;
    }

    @Override
    public Maybe<Point> writePoint(@Nonnull final Point point) {

        Objects.requireNonNull(point, "Point is required");

        return writePoint(point, defaultWriteOptions);
    }

    @Override
    public Maybe<Point> writePoint(@Nonnull final Point point, @Nonnull final WriteOptions options) {

        Objects.requireNonNull(point, "Point is required");
        Objects.requireNonNull(options, "WriteOptions is required");

        return writePoints(Flowable.just(point), options).firstElement();
    }

    @Override
    public Flowable<Point> writePoints(@Nonnull final Iterable<Point> points) {

        Objects.requireNonNull(points, "Points are required");

        return writePoints(points, defaultWriteOptions);
    }

    @Override
    public Flowable<Point> writePoints(@Nonnull final Iterable<Point> points, @Nonnull final WriteOptions options) {

        Objects.requireNonNull(points, "Points are required");
        Objects.requireNonNull(options, "WriteOptions is required");

        return writePoints(Flowable.fromIterable(points), options);
    }

    @Override
    public Flowable<Point> writePoints(@Nonnull final Publisher<Point> pointStream) {

        Objects.requireNonNull(pointStream, "Point stream is required");

        return writePoints(pointStream, defaultWriteOptions);
    }

    @Override
    public Flowable<Point> writePoints(@Nonnull final Publisher<Point> pointStream,
                                       @Nonnull final WriteOptions options) {

        Objects.requireNonNull(pointStream, "Point stream is required");
        Objects.requireNonNull(options, "WriteOptions is required");

        Flowable<Point> emitting = Flowable.fromPublisher(pointStream);
        emitting
                .map(point -> new DataPoint(point, options))
                .subscribe(processor::onNext, throwable -> publish(new UnhandledErrorEvent(throwable)));

        return emitting;
    }

    @Override
    public <M> Flowable<M> query(@Nonnull final Query query, @Nonnull final Class<M> measurementType) {

        Objects.requireNonNull(query, "Query is required");
        Objects.requireNonNull(measurementType, "Measurement type is required");

        return query(query, measurementType, QueryOptions.DEFAULTS);
    }

    @Override
    public <M> Flowable<M> query(@Nonnull final Query query,
                                 @Nonnull final Class<M> measurementType,
                                 @Nonnull final QueryOptions queryOptions) {

        Objects.requireNonNull(query, "Query is required");
        Objects.requireNonNull(measurementType, "Measurement type is required");
        Objects.requireNonNull(queryOptions, "QueryOptions is required");

        return query(Flowable.just(query), measurementType, queryOptions);
    }

    @Override
    public <M> Flowable<M> query(@Nonnull final Publisher<Query> query, @Nonnull final Class<M> measurementType) {

        Objects.requireNonNull(query, "Query publisher is required");
        Objects.requireNonNull(measurementType, "Measurement type is required");

        return query(query, measurementType, QueryOptions.DEFAULTS);
    }

    @Override
    public <M> Flowable<M> query(@Nonnull final Publisher<Query> query,
                                 @Nonnull final Class<M> measurementType,
                                 @Nonnull final QueryOptions queryOptions) {

        Objects.requireNonNull(query, "Query publisher is required");
        Objects.requireNonNull(measurementType, "Measurement type is required");
        Objects.requireNonNull(queryOptions, "QueryOptions is required");

        return query(query, queryOptions)
                .map(queryResult -> resultMapper.toPOJO(queryResult, measurementType))
                .concatMap(Flowable::fromIterable);
    }

    @Override
    public Flowable<QueryResult> query(@Nonnull final Query query) {

        Objects.requireNonNull(query, "Query is required");

        return query(query, QueryOptions.DEFAULTS);
    }

    @Override
    public Flowable<QueryResult> query(@Nonnull final Query query, @Nonnull final QueryOptions options) {

        Objects.requireNonNull(query, "Query is required");
        Objects.requireNonNull(options, "QueryOptions is required");

        return query(Flowable.just(query), options);
    }

    @Override
    public Flowable<QueryResult> query(@Nonnull final Publisher<Query> queryStream) {

        Objects.requireNonNull(queryStream, "Query publisher is required");

        return query(queryStream, QueryOptions.DEFAULTS);
    }

    @Override
    public Flowable<QueryResult> query(@Nonnull final Publisher<Query> queryStream,
                                       @Nonnull final QueryOptions options) {

        Objects.requireNonNull(queryStream, "Query publisher is required");
        Objects.requireNonNull(options, "QueryOptions is required");

        return Flowable.fromPublisher(queryStream).concatMap((Function<Query, Publisher<QueryResult>>) query -> {

            //
            // Parameters
            //
            String username = this.options.getUsername();
            String password = this.options.getPassword();
            String database = query.getDatabase();

            String precision = TimeUtil.toTimePrecision(this.options.getPrecision());

            int chunkSize = options.getChunkSize();
            String rawQuery = query.getCommandWithUrlEncoded();

            String params = "";
            if (query instanceof BoundParameterQuery) {
                params = ((BoundParameterQuery) query).getParameterJsonWithUrlEncoded();
            }
            return influxDBService
                    .query(username, password, database, precision, chunkSize, rawQuery, params)
                    .flatMap(
                            // success response
                            this::chunkReader,
                            // error response
                            throwable -> Observable.error(buildInfluxDBException(throwable)),
                            // end of response
                            Observable::empty)
                    .toFlowable(BackpressureStrategy.BUFFER);
        });
    }

    @Override
    @Nonnull
    public <T extends AbstractInfluxEvent> Observable<T> listenEvents(@Nonnull final Class<T> eventType) {

        Objects.requireNonNull(eventType, "EventType is required");

        return eventPublisher.ofType(eventType);
    }

    @Override
    public Maybe<Pong> ping() {

        SubscribeHandler onSubscribe = new SubscribeHandler();

        return influxDBService
                .ping()
                .doOnSubscribe(onSubscribe)
                .map(response -> createPong(onSubscribe.subscribeTime, response));
    }

    @Nonnull
    @Override
    public Maybe<String> version() {
        return ping().map(Pong::getVersion);
    }

    @Nonnull
    @Override
    public InfluxDBReactive enableGzip() {
        this.gzipRequestInterceptor.enable();
        return this;
    }

    @Nonnull
    @Override
    public InfluxDBReactive disableGzip() {
        this.gzipRequestInterceptor.disable();
        return this;
    }

    @Override
    public boolean isGzipEnabled() {
        return this.gzipRequestInterceptor.isEnabled();
    }

    @Override
    @Nonnull
    public InfluxDBReactive close() {

        LOG.log(Level.INFO, "Flushing any cached metrics before shutdown.");

        processor.onComplete();
        eventPublisher.onComplete();

        return this;
    }

    @Override
    public boolean isClosed() {
        return writeConsumer.isDisposed();
    }

    @Nonnull
    private FlowableTransformer<Flowable<DataPoint>, Flowable<DataPoint>> applyJitter(@Nonnull final Scheduler scheduler) {

        Objects.requireNonNull(scheduler, "Jitter scheduler is required");

        return source -> {

            //
            // source without jitter
            //
            if (batchOptions.getJitterInterval() <= 0) {
                return source;
            }

            //
            // Add jitter => dynamic delay
            //
            return source.delay((Function<Flowable<DataPoint>, Flowable<Long>>) pointFlowable -> {

                int delay = jitterDelay();

                LOG.log(Level.FINEST, "Generated Jitter dynamic delay: {0}", delay);

                return Flowable.timer(delay, TimeUnit.MILLISECONDS, scheduler);
            });
        };
    }

    private final class WritePointsConsumer implements Consumer<Flowable<DataPoint>> {

        private final Scheduler retryScheduler;

        private WritePointsConsumer(@Nonnull final Scheduler retryScheduler) {

            Objects.requireNonNull(retryScheduler, "RetryScheduler is required");

            this.retryScheduler = retryScheduler;
        }

        @Override
        public void accept(final Flowable<DataPoint> flowablePoints) {

            flowablePoints
                    .toList()
                    .filter(dataPoints -> !dataPoints.isEmpty())
                    .subscribe(dataPoints -> {

                        List<Point> points = dataPoints
                                .stream()
                                .map(dp -> dp.point)
                                .collect(Collectors.toList());

                        //
                        // Success action
                        //
                        Action success = () -> publish(new WriteSuccessEvent(points));

                        //
                        // Fail action
                        //
                        Consumer<Throwable> fail = throwable -> {

                            // HttpException is handled in retryCapabilities
                            if (throwable instanceof HttpException) {
                                return;
                            }

                            publish(new UnhandledErrorEvent(throwable));
                        };

                        //
                        // Point => InfluxDB Line Protocol
                        //
                        String body = dataPoints.stream()
                                .map(DataPoint::lineProtocol)
                                .collect(Collectors.joining("\n"));

                        //
                        // InfluxDB Line Protocol => to Request Body
                        //
                        RequestBody requestBody = RequestBody.create(options.getMediaType(), body);

                        //
                        // Parameters
                        //
                        String username = options.getUsername();
                        String password = options.getPassword();
                        String database = options.getDatabase();

                        String retentionPolicy = options.getRetentionPolicy();
                        String precision = TimeUtil.toTimePrecision(options.getPrecision());
                        String consistencyLevel = options.getConsistencyLevel().value();

                        influxDBService.writePoints(
                                username, password, database,
                                retentionPolicy, precision, consistencyLevel,
                                requestBody)
                                //
                                // Retry strategy
                                //
                                .retryWhen(retryCapabilities(points, retryScheduler))
                                .subscribe(success, fail);

                    }, throwable -> publish(new UnhandledErrorEvent(throwable)));
        }
    }

    /**
     * The retry handler that tries to retry a write if it failed previously and
     * the reason of the failure is not permanent.
     *
     * @param points         to write to InfluxDB
     * @param retryScheduler for scheduling retry write
     * @return the retry handler
     */
    @Nonnull
    private Function<Flowable<Throwable>, Publisher<?>> retryCapabilities(@Nonnull final List<Point> points,
                                                                          @Nonnull final Scheduler retryScheduler) {

        Objects.requireNonNull(retryScheduler, "RetryScheduler is required");

        return errors -> errors.flatMap(throwable -> {

            if (throwable instanceof HttpException) {

                InfluxDBException influxDBException = buildInfluxDBException(throwable);

                publish(new WriteErrorEvent(points, influxDBException));

                //
                // Retry request
                //
                if (influxDBException.isRetryWorth()) {

                    int retryInterval = batchOptions.getRetryInterval() + jitterDelay();

                    return Flowable.just("notify").delay(retryInterval, TimeUnit.MILLISECONDS, retryScheduler);
                }
            }

            //
            // This type of throwable is not able to retry
            //
            return Flowable.error(throwable);
        });
    }

    @Nonnull
    private InfluxDBException buildInfluxDBException(@Nonnull final Throwable throwable) {

        Objects.requireNonNull(throwable, "Throwable is required");

        if (throwable instanceof HttpException) {

            String errorMessage = ((HttpException) throwable).response().headers().get("X-Influxdb-Error");
            if (errorMessage != null) {
                return InfluxDBException.buildExceptionFromErrorMessage(errorMessage);
            }
        }

        return new InfluxDBException(throwable);
    }

    private int jitterDelay() {

        return (int) (Math.random() * batchOptions.getJitterInterval());
    }

    @Nonnull
    private Observable<QueryResult> chunkReader(@Nonnull final ResponseBody body) {

        Objects.requireNonNull(body, "ResponseBody is required");

        return Observable.create(subscriber -> {

            boolean isCompleted = false;
            try {
                BufferedSource source = body.source();

                //
                // Subscriber is not disposed && source has data => parse
                //
                while (!subscriber.isDisposed() && !source.exhausted()) {

                    QueryResult queryResult = InfluxDBReactiveImpl.this.adapter.fromJson(source);
                    if (queryResult != null) {

                        subscriber.onNext(queryResult);
                        publish(new QueryParsedResponseEvent(source, queryResult));
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

    private <T extends AbstractInfluxEvent> void publish(@Nonnull final T event) {

        Objects.requireNonNull(event, "Event is required");

        eventPublisher.onNext(event);
    }

    private class DataPoint {

        private Point point;
        private WriteOptions options;

        private DataPoint(@Nonnull final Point point, @Nonnull final WriteOptions options) {
            this.point = point;
            this.options = options;
        }

        @Nonnull
        private String lineProtocol() {
            return point.lineProtocol();
        }
    }

    private class SubscribeHandler implements Consumer<Disposable> {

        private Long subscribeTime;

        @Override
        public void accept(final Disposable disposable) {
            subscribeTime = System.currentTimeMillis();
        }
    }

    /**
     * Just a proof of concept.
     * <p>
     * TODO caching, testing, ...
     */
    private static class MeasurementToPoint<M> implements Function<M, Point> {

        private static final Logger LOG = Logger.getLogger(MeasurementToPoint.class.getName());

        @Override
        public Point apply(final M measurement) {

            Measurement def = measurement.getClass().getAnnotation(Measurement.class);

            Point.Builder point = Point.measurement(def.name());

            for (Field field : measurement.getClass().getDeclaredFields()) {

                Column column = field.getAnnotation(Column.class);
                if (column != null) {

                    String name = column.name();
                    Object value = null;

                    try {
                        field.setAccessible(true);
                        value = field.get(measurement);
                    } catch (IllegalAccessException e) {

                        LOG.log(Level.WARNING,
                                "Field {0} of {1} is not accessible",
                                new Object[]{field.getName(), measurement});
                    }

                    if (value == null) {
                        LOG.log(Level.FINEST, "Field {0} of {1} has null value",
                                new Object[]{field.getName(), measurement});

                        continue;
                    }

                    if (column.tag()) {
                        point.tag(name, value.toString());
                    } else if (Instant.class.isAssignableFrom(field.getType())) {

                        point.time(((Instant) value).toEpochMilli(), def.timeUnit());
                    } else {

                        if (Double.class.isAssignableFrom(value.getClass())) {
                            point.addField(name, (Double) value);
                        } else if (Number.class.isAssignableFrom(value.getClass())) {

                            point.addField(name, (Number) value);
                        } else if (String.class.isAssignableFrom(value.getClass())) {

                            point.addField(name, (String) value);
                        }
                    }
                }
            }

            return point.build();
        }
    }
}
